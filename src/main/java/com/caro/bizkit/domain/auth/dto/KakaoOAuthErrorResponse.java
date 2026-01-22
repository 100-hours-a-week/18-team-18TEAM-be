package com.caro.bizkit.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoOAuthErrorResponse(
        @JsonProperty("error") String error,
        @JsonProperty("error_description") String errorDescription,
        @JsonProperty("error_code") String errorCode
) {
}
