package com.caro.bizkit.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiJobAnalyzeResponse(
        String message,
        Data data
) {
    public record Data(
            String introduction,
            @JsonProperty("search_confidence") Double searchConfidence
    ) {}
}
