package com.example.caro.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount,
        @JsonProperty("properties") KakaoProperties properties
) {
    public record KakaoAccount(
            @JsonProperty("email") String email,
            @JsonProperty("profile") KakaoProfile profile
    ) {
    }

    public record KakaoProperties(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("profile_image") String profileImage
    ) {
    }

    public record KakaoProfile(@JsonProperty("profile_image_url") String profileImageUrl) {
    }

}
