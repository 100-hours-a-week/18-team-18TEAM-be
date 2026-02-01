package com.caro.bizkit.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiJobAnalyzeRequest(
        @JsonProperty("user_id") Integer userId,
        String name,
        String company,
        String department,
        String position,
        List<ProjectDto> projects,
        List<AwardDto> awards
) {
    public record ProjectDto(
            String name,
            String content,
            @JsonProperty("period_months") Integer periodMonths
    ) {}

    public record AwardDto(
            String name,
            Integer year
    ) {}
}
