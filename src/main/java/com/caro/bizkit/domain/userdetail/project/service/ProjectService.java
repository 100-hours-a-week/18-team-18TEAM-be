package com.caro.bizkit.domain.userdetail.project.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.userdetail.project.dto.ProjectRequest;
import com.caro.bizkit.domain.userdetail.project.dto.ProjectResponse;
import com.caro.bizkit.domain.userdetail.project.entity.Project;
import com.caro.bizkit.domain.userdetail.project.repository.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
