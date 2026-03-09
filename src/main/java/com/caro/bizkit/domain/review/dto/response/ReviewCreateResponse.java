package com.caro.bizkit.domain.review.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReviewCreateResponse(
        @JsonProperty("review_id") Integer reviewId
) {}
