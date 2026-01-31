package com.caro.bizkit.domain.ai.event;

import java.time.LocalDateTime;

public record UserProfileUpdatedEvent(
        Integer userId,
        String updateType,
        LocalDateTime occurredAt
) {}
