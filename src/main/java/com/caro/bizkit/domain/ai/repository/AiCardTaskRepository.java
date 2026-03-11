package com.caro.bizkit.domain.ai.repository;

import com.caro.bizkit.domain.ai.entity.AiCardTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiCardTaskRepository extends JpaRepository<AiCardTask, Integer> {

    boolean existsByUser_IdAndStatusIn(Integer userId, java.util.List<com.caro.bizkit.domain.ai.entity.AiAnalysisStatus> statuses);

    Optional<AiCardTask> findTopByUser_IdOrderByCreatedAtDesc(Integer userId);
}
