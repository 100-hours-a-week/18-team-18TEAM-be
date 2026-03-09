package com.caro.bizkit.domain.review.service;

import com.caro.bizkit.common.exception.CustomException;
import com.caro.bizkit.domain.review.dto.request.ReviewCreateRequest;
import com.caro.bizkit.domain.review.dto.response.TagResponse;
import com.caro.bizkit.domain.review.entity.Review;
import com.caro.bizkit.domain.review.entity.ReviewTag;
import com.caro.bizkit.domain.review.entity.Tag;
import com.caro.bizkit.domain.review.repository.ReviewRepository;
import com.caro.bizkit.domain.review.repository.ReviewTagRepository;
import com.caro.bizkit.domain.review.repository.TagRepository;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final TagRepository tagRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TagResponse> getTags() {
        return tagRepository.findAll().stream()
                .map(TagResponse::from)
                .toList();
    }

    @Transactional
    public Map<String, Integer> createReview(UserPrincipal principal, ReviewCreateRequest request) {
        if (principal.id().equals(request.revieweeId())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "본인에게 리뷰를 작성할 수 없습니다.");
        }
        if (reviewRepository.existsByReviewer_IdAndReviewee_Id(principal.id(), request.revieweeId())) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 리뷰를 작성했습니다.");
        }

        User reviewer = userRepository.getReferenceById(principal.id());
        User reviewee = userRepository.getReferenceById(request.revieweeId());
        Review review = reviewRepository.save(Review.create(reviewer, reviewee, request.score(), request.comment()));

        request.tagIdList().forEach(tagId -> {
            Tag tag = tagRepository.getReferenceById(tagId);
            reviewTagRepository.save(ReviewTag.create(review, tag));
        });

        return Map.of("review_id", review.getId());
    }
}
