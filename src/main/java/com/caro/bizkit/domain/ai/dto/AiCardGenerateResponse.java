package com.caro.bizkit.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiCardGenerateResponse(
        String message,
        Data data
) {
    public record Data(
            @JsonProperty("image_data_url") String imageDataUrl,
            @JsonProperty("image_url") String imageUrl,
            Integer width,
            Integer height,
            @JsonProperty("style_tag") String styleTag
    ) {}
}
