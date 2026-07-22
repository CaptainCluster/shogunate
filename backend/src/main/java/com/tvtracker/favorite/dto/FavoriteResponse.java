package com.tvtracker.favorite.dto;

import java.time.Instant;
import java.util.UUID;

public record FavoriteResponse(UUID id, UUID showId, Instant createdAt) {}
