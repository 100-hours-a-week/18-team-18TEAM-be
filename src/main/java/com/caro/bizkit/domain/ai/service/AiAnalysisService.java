package com.caro.bizkit.domain.ai.service;

import com.caro.bizkit.domain.ai.client.AiAnalysisClient;
import com.caro.bizkit.domain.ai.config.AiClientProperties;
import com.caro.bizkit.domain.ai.dto.AiJobAnalyzeRequest;
import com.caro.bizkit.domain.ai.dto.AiJobSubmitResponse;
import com.caro.bizkit.domain.ai.dto.AiJobAnalyzeResponse;
import com.caro.bizkit.domain.ai.dto.AiTaskStatusResponse;
import com.caro.bizkit.domain.ai.entity.AiAnalysisTask;
import com.caro.bizkit.domain.ai.entity.AiAnalysisTaskType;
import com.caro.bizkit.domain.ai.repository.AiAnalysisTaskRepository;
import com.caro.bizkit.domain.card.entity.Card;
import com.caro.bizkit.domain.card.repository.CardRepository;
import com.caro.bizkit.domain.userdetail.activity.entity.Activity;
import com.caro.bizkit.domain.userdetail.activity.repository.ActivityRepository;
import com.caro.bizkit.domain.userdetail.project.entity.Project;
import com.caro.bizkit.domain.userdetail.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {

    private static final long DEBOUNCE_SECONDS = 10;

    private final ConcurrentHashMap<Integer, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final AiAnalysisClient aiClient;
    private final CardRepository cardRepository;
    private final ProjectRepository projectRepository;
    private final ActivityRepository activityRepository;
    private final AiAnalysisTaskRepository taskRepository;
    private final TransactionTemplate transactionTemplate;
    private final AiClientProperties properties;

    public void addToBatch(Integer cardId) {
        pendingTasks.compute(cardId, (id, existing) -> {
            if (existing != null && !existing.isDone()) {
                existing.cancel(false);
                log.info("Card {} 디바운스 리셋 (10초 재시작)", id);
            }
            return scheduler.schedule(() -> {
                pendingTasks.remove(id);
                processCard(id);
            }, DEBOUNCE_SECONDS, TimeUnit.SECONDS);
        });
        log.info("Card {} AI 분석 예약 ({}초 후)", cardId, DEBOUNCE_SECONDS);
    }

    private void processCard(Integer cardId) {
        // ① Tx: 데이터 조회 + task 생성
        Integer[] taskDbId = new Integer[1];
        AiJobAnalyzeRequest[] requestRef = new AiJobAnalyzeRequest[1];

        transactionTemplate.executeWithoutResult(status -> {
            Card card = cardRepository.findById(cardId).orElse(null);
            if (card == null || card.getUser() == null) {
                log.warn("Card {} 조회 실패 또는 익명 명함, AI 분석 건너뜀", cardId);
                return;
            }
            Integer userId = card.getUser().getId();
            List<Project> projects = projectRepository.findAllByUserId(userId);
            List<Activity> activities = activityRepository.findAllByUserId(userId);

            AiAnalysisTask task = AiAnalysisTask.create(card.getUser(), AiAnalysisTaskType.JOB);
            taskRepository.save(task);
            taskDbId[0] = task.getId();
            requestRef[0] = buildRequest(card, projects, activities);
        });

        if (taskDbId[0] == null) return;

        // ② No Tx: POST /ai/job/analyze
        AiJobSubmitResponse submitResponse;
        try {
            submitResponse = aiClient.submitAnalysis(requestRef[0]);
        } catch (Exception e) {
            log.error("Card {} AI 분석 요청 실패: {}", cardId, e.getMessage());
            transactionTemplate.executeWithoutResult(status ->
                    taskRepository.findById(taskDbId[0]).ifPresent(AiAnalysisTask::fail));
            return;
        }

        String aiTaskId = submitResponse.taskId();
        log.info("Card {} AI 분석 요청 완료, aiTaskId={}", cardId, aiTaskId);

        // ③ Tx: aiTaskId 저장
        transactionTemplate.executeWithoutResult(status ->
                taskRepository.findById(taskDbId[0]).ifPresent(task -> task.assignAiTaskId(aiTaskId)));

        // ④ polling 시작
        long deadline = System.currentTimeMillis() + properties.getJob().getTimeoutSeconds() * 1000L;
        scheduler.schedule(() -> poll(cardId, taskDbId[0], aiTaskId, deadline),
                properties.getJob().getPollIntervalSeconds(), TimeUnit.SECONDS);
    }

    private void poll(Integer cardId, Integer taskDbId, String aiTaskId, long deadline) {
        if (System.currentTimeMillis() > deadline) {
            log.warn("Card {} AI 분석 시간 초과 (aiTaskId={})", cardId, aiTaskId);
            transactionTemplate.executeWithoutResult(status ->
                    taskRepository.findById(taskDbId).ifPresent(AiAnalysisTask::fail));
            return;
        }

        try {
            AiTaskStatusResponse statusResponse = aiClient.getTaskStatus(aiTaskId);

            if ("done".equals(statusResponse.progress())) {
                Optional<AiJobAnalyzeResponse> result = aiClient.getTaskResult(aiTaskId);
                result.ifPresent(res -> transactionTemplate.executeWithoutResult(status -> {
                    cardRepository.findById(cardId).ifPresent(card ->
                            card.updateDescription(res.data().introduction()));
                    taskRepository.findById(taskDbId).ifPresent(AiAnalysisTask::complete);
                }));
                log.info("Card {} AI 분석 완료", cardId);
            } else if ("failed".equals(statusResponse.status())) {
                log.warn("Card {} AI 분석 실패 (aiTaskId={})", cardId, aiTaskId);
                transactionTemplate.executeWithoutResult(status ->
                        taskRepository.findById(taskDbId).ifPresent(AiAnalysisTask::fail));
            } else {
                scheduler.schedule(() -> poll(cardId, taskDbId, aiTaskId, deadline),
                        properties.getJob().getPollIntervalSeconds(), TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("Card {} polling 오류: {}", cardId, e.getMessage());
            transactionTemplate.executeWithoutResult(status ->
                    taskRepository.findById(taskDbId).ifPresent(AiAnalysisTask::fail));
        }
    }

    private AiJobAnalyzeRequest buildRequest(Card card, List<Project> projects, List<Activity> activities) {
        List<AiJobAnalyzeRequest.ProjectDto> projectDtos = projects.stream()
                .map(this::toProjectDto)
                .toList();

        List<AiJobAnalyzeRequest.AwardDto> awardDtos = activities.stream()
                .map(this::toAwardDto)
                .toList();

        return new AiJobAnalyzeRequest(
                card.getId(),
                card.getName(),
                card.getCompany(),
                card.getDepartment(),
                card.getPosition(),
                projectDtos,
                awardDtos
        );
    }

    private AiJobAnalyzeRequest.ProjectDto toProjectDto(Project project) {
        Integer periodMonths = calculatePeriodMonths(project.getStartDate(), project.getEndDate());
        return new AiJobAnalyzeRequest.ProjectDto(
                project.getName(),
                project.getContent(),
                periodMonths
        );
    }

    private AiJobAnalyzeRequest.AwardDto toAwardDto(Activity activity) {
        Integer year = activity.getWinDate() != null ? activity.getWinDate().getYear() : null;
        return new AiJobAnalyzeRequest.AwardDto(
                activity.getName(),
                year
        );
    }

    private Integer calculatePeriodMonths(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            return null;
        }
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return (int) ChronoUnit.MONTHS.between(startDate, end);
    }
}
