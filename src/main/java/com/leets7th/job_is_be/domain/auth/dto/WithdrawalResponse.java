package com.leets7th.job_is_be.domain.auth.dto;

import java.time.LocalDateTime;

public record WithdrawalResponse(
        LocalDateTime restorableUntil
) {
}
