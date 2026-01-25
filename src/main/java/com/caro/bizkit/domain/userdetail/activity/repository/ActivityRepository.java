package com.caro.bizkit.domain.userdetail.activity.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.userdetail.activity.entity.Activity;
import com.caro.bizkit.domain.userdetail.project.entity.Project;

import java.util.List;

public interface ActivityRepository extends BaseRepository<Activity, Integer> {
    List<Activity> findAllByUserId(Integer userId);
}
