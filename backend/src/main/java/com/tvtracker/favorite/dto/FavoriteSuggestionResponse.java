package com.tvtracker.favorite.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FavoriteSuggestionResponse(UUID showId, BigDecimal weightedScore) {}
