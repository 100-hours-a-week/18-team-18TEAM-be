package com.caro.bizkit.domain.review.controller;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.domain.review.dto.request.ReviewCreateRequest;
import com.caro.bizkit.domain.review.dto.response.ReviewDetailResponse;
import com.caro.bizkit.domain.review.dto.response.TagResponse;
import com.caro.bizkit.domain.review.service.ReviewService;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        return ResponseEntity.ok(ApiResponse.success("태그 조회 성공", reviewService.getTags()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> getMyReview(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam Integer revieweeId
    ) {
        return ResponseEntity.ok(ApiResponse.success("내가 쓴 리뷰 조회 성공", reviewService.getMyReview(user, revieweeId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Integer>>> createReview(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success("리뷰 작성 성공", reviewService.createReview(user, request)));
    }
}
