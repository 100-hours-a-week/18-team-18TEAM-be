package com.caro.bizkit.domain.user.repository;

import com.caro.bizkit.domain.user.entity.AiUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiUsageRepository extends JpaRepository<AiUsage, Integer> {
    void deleteByUserId(Integer userId);
}
