package com.caro.bizkit.domain.card.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CardRequest(
        @NotBlank
        @Size(max = 30)
        String name,
        @NotBlank
        @Email
        String email,
        @Size(max = 15)
        String phone_number,
        @Size(max = 15)
        String lined_number,
        @NotBlank
        @Size(max = 20)
        String company,
        @Size(max = 20)
        String position,
        @Size(max = 20)
        String department,
        @NotNull
        LocalDate start_date,
        LocalDate end_date,
        Boolean is_progress,
        @Size(max = 500)
        String ai_image_key
) {
}
