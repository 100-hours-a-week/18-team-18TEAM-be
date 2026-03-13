package com.caro.bizkit.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AiCardGenerationRequest(
        @NotNull @JsonProperty("card_id") Integer cardId,
        @NotBlank String tag,
        String text
) {}
