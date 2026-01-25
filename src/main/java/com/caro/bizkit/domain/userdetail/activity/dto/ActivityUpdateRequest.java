package com.caro.bizkit.domain.userdetail.activity.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ActivityUpdateRequest(
        @Size(max = 50)
        String name,
        @Size(max = 50)
        String grade,
        @Size(max = 50)
        String organization,
        @Size(max = 2000)
        String content,
        LocalDate win_date
) {
}
