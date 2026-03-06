package com.caro.bizkit.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiJobSubmitResponse(
        @JsonProperty("task_id") String taskId,
        String status,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("poll_url") String pollUrl
) {}
