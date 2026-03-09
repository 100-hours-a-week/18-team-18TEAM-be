package com.caro.bizkit.domain.review.service;

import com.caro.bizkit.common.exception.CustomException;
import com.caro.bizkit.domain.review.dto.request.ReviewCreateRequest;
import com.caro.bizkit.domain.review.dto.response.ReviewDetailResponse;
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

    @Transactional
    public void updateReview(UserPrincipal principal, Map<String, Object> body) {
        Integer revieweeId = (Integer) body.get("reviewee_id");
        Review review = reviewRepository.findByReviewer_IdAndReviewee_Id(principal.id(), revieweeId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        if (!review.getReviewer().getId().equals(principal.id())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정할 수 있습니다.");
        }
        applyReviewUpdates(review, body);
    }

    private void applyReviewUpdates(Review review, Map<String, Object> body) {
        if (body.containsKey("score")) review.update((Integer) body.get("score"), null);
        if (body.containsKey("comment")) review.update(null, (String) body.get("comment"));
        if (body.containsKey("tag_id_list")) {
            List<?> rawList = (List<?>) body.get("tag_id_list");
            if (rawList.size() < 1 || rawList.size() > 3) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "태그는 1~3개 선택해야 합니다.");
            }
            reviewTagRepository.deleteAllByReviewId(review.getId());
            rawList.stream()
                    .map(id -> (Integer) id)
                    .forEach(tagId -> reviewTagRepository.save(ReviewTag.create(review, tagRepository.getReferenceById(tagId))));
        }
    }

    @Transactional
    public void deleteReview(UserPrincipal principal, Integer revieweeId) {
        Review review = reviewRepository.findByReviewer_IdAndReviewee_Id(principal.id(), revieweeId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        if (!review.getReviewer().getId().equals(principal.id())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "본인의 리뷰만 삭제할 수 있습니다.");
        }
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public ReviewDetailResponse getMyReview(UserPrincipal principal, Integer revieweeId) {
        return reviewRepository.findByReviewer_IdAndReviewee_Id(principal.id(), revieweeId)
                .map(review -> ReviewDetailResponse.of(review, reviewTagRepository.findAllByReview_Id(review.getId())))
                .orElse(null);
    }
}
