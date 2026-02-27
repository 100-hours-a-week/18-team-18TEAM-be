package com.caro.bizkit.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiTaskStatusResponse(
        @JsonProperty("task_id") String taskId,
        @JsonProperty("task_type") String taskType,
        String status,
        String progress,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("started_at") String startedAt,
        @JsonProperty("completed_at") String completedAt,
        String error
) {}
