package com.tvtracker.favorite.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddFavoriteRequest(@NotNull UUID showId) {}
