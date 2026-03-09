package com.caro.bizkit.domain.review.repository;

import com.caro.bizkit.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    boolean existsByReviewer_IdAndReviewee_Id(Integer reviewerId, Integer revieweeId);

    java.util.Optional<Review> findByReviewer_IdAndReviewee_Id(Integer reviewerId, Integer revieweeId);
}
