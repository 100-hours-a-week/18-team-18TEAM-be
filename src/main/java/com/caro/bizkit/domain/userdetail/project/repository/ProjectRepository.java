package com.caro.bizkit.domain.userdetail.project.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.userdetail.project.entity.Project;
import java.util.List;

public interface ProjectRepository extends BaseRepository<Project, Integer> {
    List<Project> findAllByUserId(Integer userId);
}
