package com.example.caro.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String code) {
}
