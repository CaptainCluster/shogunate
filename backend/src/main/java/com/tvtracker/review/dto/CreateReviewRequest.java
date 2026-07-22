package com.tvtracker.review.dto;

import com.tvtracker.common.TargetType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateReviewRequest(
        @NotNull TargetType targetType, @NotNull UUID targetId, @NotNull BigDecimal rating, String body) {}
