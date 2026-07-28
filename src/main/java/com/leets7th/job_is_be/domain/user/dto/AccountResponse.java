package com.leets7th.job_is_be.domain.user.dto;

import com.leets7th.job_is_be.domain.user.enums.SocialType;

import java.time.LocalDateTime;

public record AccountResponse(
        SocialType socialType,
        LocalDateTime joinedAt,
        String receivingEmail,
        boolean emailVerified
) {
}
