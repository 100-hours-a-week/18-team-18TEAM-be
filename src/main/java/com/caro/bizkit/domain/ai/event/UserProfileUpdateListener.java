package com.caro.bizkit.domain.ai.event;

import com.caro.bizkit.domain.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileUpdateListener {

    private final AiAnalysisService aiAnalysisService;

    @EventListener
    public void handleUserProfileUpdated(UserProfileUpdatedEvent event) {
        log.info("Received UserProfileUpdatedEvent: userId={}, type={}", event.userId(), event.updateType());
        aiAnalysisService.addToBatch(event.userId());
    }
}
