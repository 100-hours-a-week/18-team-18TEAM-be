package com.caro.bizkit.domain.userdetail.project.service;

import com.caro.bizkit.domain.ai.event.UserProfileUpdatedEvent;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.userdetail.project.dto.ProjectRequest;
import com.caro.bizkit.domain.userdetail.project.dto.ProjectResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Consumer;
import com.caro.bizkit.domain.userdetail.project.entity.Project;
import com.caro.bizkit.domain.userdetail.project.repository.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects(UserPrincipal principal) {
        return projectRepository.findAllByUserId(principal.id()).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByUserId(Integer userId) {
        return projectRepository.findAllByUserId(userId).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional
    public ProjectResponse createMyProject(UserPrincipal principal, ProjectRequest request) {
        User user = userRepository.getReferenceById(principal.id());
        Project project = Project.create(
                user,
                request.name(),
                request.content(),
                request.start_date(),
                request.end_date()
        );
        Project saved = projectRepository.save(project);

        eventPublisher.publishEvent(new UserProfileUpdatedEvent(
                principal.id(), "PROJECT", LocalDateTime.now()
        ));

        return ProjectResponse.from(saved);
    }

    @Transactional
    @PreAuthorize("@projectSecurity.isOwner(#projectId, authentication)")
    public ProjectResponse updateMyProject(
            UserPrincipal principal,
            Integer projectId,
            Map<String, Object> request
    ) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (request == null) {
            return ProjectResponse.from(project);
        }

        applyUpdates(project, request);

        eventPublisher.publishEvent(new UserProfileUpdatedEvent(
                principal.id(), "PROJECT", LocalDateTime.now()
        ));

        return ProjectResponse.from(project);
    }

    private void applyUpdates(Project project, Map<String, Object> request) {
        applyIfPresent(request, "name", project::updateName);
        applyIfPresent(request, "content", project::updateContent);
        applyDateIfPresent(request, "start_date", project::updateStartDate);

        if (request.containsKey("is_progress")) {
            Boolean isProgress = (Boolean) request.get("is_progress");
            project.updateIsProgress(isProgress);
            if (Boolean.TRUE.equals(isProgress)) {
                project.updateEndDate(null);
            }
        }

        if (request.containsKey("end_date")) {
            Object value = request.get("end_date");
            if (value == null) {
                project.updateEndDate(null);
                project.updateIsProgress(Boolean.TRUE);
            } else {
                LocalDate endDate = value instanceof LocalDate ? (LocalDate) value : LocalDate.parse((String) value);
                project.updateEndDate(endDate);
                project.updateIsProgress(Boolean.FALSE);
            }
        }
    }

    private void applyIfPresent(Map<String, Object> request, String key, Consumer<String> updater) {
        if (request.containsKey(key)) {
            updater.accept((String) request.get(key));
        }
    }

    private void applyDateIfPresent(Map<String, Object> request, String key, Consumer<LocalDate> updater) {
        if (request.containsKey(key)) {
            Object value = request.get(key);
            if (value instanceof LocalDate) {
                updater.accept((LocalDate) value);
            } else if (value instanceof String) {
                updater.accept(LocalDate.parse((String) value));
            }
        }
    }

    @Transactional
    @PreAuthorize("@projectSecurity.isOwner(#projectId, authentication)")
    public void deleteMyProject(UserPrincipal principal, Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        projectRepository.delete(project);

        eventPublisher.publishEvent(new UserProfileUpdatedEvent(
                principal.id(), "PROJECT", LocalDateTime.now()
        ));
    }
}
