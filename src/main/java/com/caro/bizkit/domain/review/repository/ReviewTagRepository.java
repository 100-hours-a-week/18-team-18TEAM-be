package com.caro.bizkit.domain.review.repository;

import com.caro.bizkit.domain.review.entity.ReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewTagRepository extends JpaRepository<ReviewTag, Integer> {
}
