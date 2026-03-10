package com.caro.bizkit.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiHexAnalyzeResponse(
        String message,
        Data data
) {
    public record Data(
            @JsonProperty("radar_chart") RadarChart radarChart,
            @JsonProperty("confidence_level") String confidenceLevel,
            @JsonProperty("analysis_summary") AnalysisSummary analysisSummary
    ) {}

    public record RadarChart(
            Integer collaboration,
            Integer communication,
            Integer technical,
            Integer documentation,
            Integer reliability,
            Integer preference
    ) {}

    public record AnalysisSummary(
            String collaboration,
            String communication,
            String technical,
            String documentation,
            String reliability,
            String preference
    ) {}
}
