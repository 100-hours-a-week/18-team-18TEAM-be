package com.caro.bizkit.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiCardGenerateRequest(
        @JsonProperty("user_id") Integer userId,
        @JsonProperty("card_info") CardInfo cardInfo,
        Style style
) {
    public record CardInfo(
            String name,
            String company,
            String department,
            String position,
            String phone,
            String email,
            String address
    ) {}

    public record Style(
            String tag,
            String text
    ) {}
}
