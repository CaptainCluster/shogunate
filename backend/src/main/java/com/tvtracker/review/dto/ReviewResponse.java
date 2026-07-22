package com.tvtracker.review.dto;

import com.tvtracker.common.TargetType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        TargetType targetType,
        UUID targetId,
        BigDecimal rating,
        String body,
        Instant createdAt,
        Instant updatedAt) {}
