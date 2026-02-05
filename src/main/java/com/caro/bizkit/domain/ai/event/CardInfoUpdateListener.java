package com.caro.bizkit.domain.ai.event;

import com.caro.bizkit.domain.ai.service.AiAnalysisService;
import com.caro.bizkit.domain.card.entity.Card;
import com.caro.bizkit.domain.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardInfoUpdateListener {

    private final AiAnalysisService aiAnalysisService;
    private final CardRepository cardRepository;

    @EventListener
    public void handleCardInfoUpdated(CardInfoUpdatedEvent event) {
        log.info("Received CardInfoUpdatedEvent: id={}, type={}", event.resourceId(), event.updateType());

        if ("CARD".equals(event.updateType())) {
            aiAnalysisService.addToBatch(event.resourceId());
        } else {
            Optional<Card> card = cardRepository.findTopByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(event.resourceId());
            if (card.isPresent()) {
                aiAnalysisService.addToBatch(card.get().getId());
            } else {
                log.warn("No card found for userId={}, skipping AI analysis", event.resourceId());
            }
        }
    }
}
