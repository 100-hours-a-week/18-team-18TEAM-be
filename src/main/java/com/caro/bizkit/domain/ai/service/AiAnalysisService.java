package com.caro.bizkit.domain.ai.service;

import com.caro.bizkit.common.exception.CustomException;
import com.caro.bizkit.domain.ai.client.AiAnalysisClient;
import com.caro.bizkit.domain.ai.dto.AiJobAnalyzeRequest;
import com.caro.bizkit.domain.ai.dto.AiJobAnalyzeResponse;
import com.caro.bizkit.domain.ai.entity.AiAnalysisTask;
import com.caro.bizkit.domain.ai.repository.AiAnalysisTaskRepository;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.userdetail.activity.entity.Activity;
import com.caro.bizkit.domain.userdetail.activity.repository.ActivityRepository;
import com.caro.bizkit.domain.userdetail.project.entity.Project;
import com.caro.bizkit.domain.userdetail.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {

    private final ConcurrentHashMap<Integer, LocalDateTime> pendingUsers = new ConcurrentHashMap<>();

    private final AiAnalysisClient aiClient;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ActivityRepository activityRepository;
    private final AiAnalysisTaskRepository taskRepository;
    private final TransactionTemplate transactionTemplate;

    public void addToBatch(Integer userId) {
        pendingUsers.put(userId, LocalDateTime.now());
        log.info("Adding user to batch: {}", userId);
    }

    @Scheduled(fixedDelay = 300000)
    public void processBatch() {
        Set<Integer> userIds = new HashSet<>(pendingUsers.keySet());
        pendingUsers.clear();

        if (userIds.isEmpty()) {
            return;
        }

        log.info("Processing batch: {} users", userIds.size());

        for (Integer userId : userIds) {
            processUserWithTransaction(userId);
        }
    }

    private void processUserWithTransaction(Integer userId) {
        transactionTemplate.executeWithoutResult(status -> {
            AiAnalysisTask task = null;
            try {
                User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow();
                List<Project> projects = projectRepository.findAllByUserId(userId);
                List<Activity> activities = activityRepository.findAllByUserId(userId);

                task = AiAnalysisTask.create(user);
                taskRepository.save(task);

                AiJobAnalyzeRequest request = buildRequest(user, projects, activities);
                AiJobAnalyzeResponse response = aiClient.analyzeSync(request);

                if (response != null && response.data() != null) {
                    user.updateDescription(response.data().introduction());
                    task.complete();
                    log.info("AI analysis completed for user: {}", userId);
                } else {
                    task.fail();
                    log.warn("AI analysis returned empty response for user: {}", userId);
                }
            } catch (CustomException e) {
                if (task != null) {
                    task.fail();
                }
                log.error("AI server error for user {}: status={}, message={}",
                        userId, e.getStatus(), e.getMessage());
            } catch (Exception e) {
                if (task != null) {
                    task.fail();
                }
                log.error("AI analysis failed for user: {}", userId, e);
            }
        });
    }

    private AiJobAnalyzeRequest buildRequest(User user, List<Project> projects, List<Activity> activities) {
        List<AiJobAnalyzeRequest.ProjectDto> projectDtos = projects.stream()
                .map(this::toProjectDto)
                .toList();

        List<AiJobAnalyzeRequest.AwardDto> awardDtos = activities.stream()
                .map(this::toAwardDto)
                .toList();

        return new AiJobAnalyzeRequest(
                user.getId(),
                user.getName(),
                user.getCompany(),
                user.getDepartment(),
                user.getPosition(),
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
