package com.caro.bizkit.domain.userdetail.project.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.userdetail.project.dto.ProjectResponse;
import com.caro.bizkit.domain.userdetail.project.repository.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<ProjectResponse> getMyProjects(UserPrincipal principal) {

        return projectRepository.findAllByUserId(principal.id()).stream()
                .map(ProjectResponse::from)
                .toList();
    }
}
