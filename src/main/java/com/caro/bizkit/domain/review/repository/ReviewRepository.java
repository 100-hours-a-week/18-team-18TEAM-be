package com.caro.bizkit.domain.review.repository;

import com.caro.bizkit.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
}
