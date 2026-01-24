package com.caro.bizkit.domain.withdrawl.dto;

import jakarta.validation.constraints.NotNull;

public record WithdrawalRequest(@NotNull Integer reason_id) {
}
