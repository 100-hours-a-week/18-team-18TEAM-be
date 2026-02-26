package com.caro.bizkit.domain.card.dto;

public record CardCreateResult(
        CardResponse card,
        boolean isDuplicate
) {
}
