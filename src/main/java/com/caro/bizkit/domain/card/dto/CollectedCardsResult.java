package com.caro.bizkit.domain.card.dto;

import java.util.List;

public record CollectedCardsResult(
        List<CardResponse> data,
        Integer cursorId,
        boolean hasNext
) {
}
