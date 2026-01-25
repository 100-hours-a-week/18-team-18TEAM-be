package com.caro.bizkit.domain.userdetail.project.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.userdetail.project.entity.Project;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends BaseRepository<Project, Integer> {
    List<Project> findAllByUserId(Integer userId);
    Optional<Project> findByIdAndUserId(Integer id, Integer userId);
}
