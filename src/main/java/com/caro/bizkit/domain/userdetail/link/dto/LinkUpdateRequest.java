package com.caro.bizkit.domain.userdetail.link.dto;

import jakarta.validation.constraints.Size;

public record LinkUpdateRequest(
        @Size(max = 100)
        String title,
        @Size(max = 2048)
        String link
) {
}
