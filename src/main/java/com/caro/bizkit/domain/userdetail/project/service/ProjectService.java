package com.caro.bizkit.domain.userdetail.project.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.userdetail.project.dto.ProjectRequest;
import com.caro.bizkit.domain.userdetail.project.dto.ProjectResponse;
import com.caro.bizkit.domain.userdetail.project.dto.ProjectUpdateRequest;
import com.caro.bizkit.domain.userdetail.project.entity.Project;
import com.caro.bizkit.domain.userdetail.project.repository.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<ProjectResponse> getMyProjects(UserPrincipal principal) {
        return projectRepository.findAllByUserId(principal.id()).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    public List<ProjectResponse> getProjectsByUserId(Integer userId) {
        return projectRepository.findAllByUserId(userId).stream()
                .map(ProjectResponse::from)
                .toList();
    }

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
        return ProjectResponse.from(saved);
    }

    @PreAuthorize("@projectSecurity.isOwner(#projectId, authentication)")
    public ProjectResponse updateMyProject(
            UserPrincipal principal,
            Integer projectId,
            ProjectUpdateRequest request
    ) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (request.name() != null) {
            project.updateName(request.name());
        }
        if (request.content() != null) {
            project.updateContent(request.content());
        }
        if (request.start_date() != null) {
            project.updateStartDate(request.start_date());
        }
        if (request.is_progress() != null) {
            project.updateIsProgress(request.is_progress());
            if (request.is_progress()) {
                project.updateEndDate(null);
            }
        }
        if (request.end_date() != null) {
            project.updateEndDate(request.end_date());
            project.updateIsProgress(Boolean.FALSE);
        }
        return ProjectResponse.from(project);
    }

    @PreAuthorize("@projectSecurity.isOwner(#projectId, authentication)")
    public void deleteMyProject(UserPrincipal principal, Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        projectRepository.delete(project);
    }
}
