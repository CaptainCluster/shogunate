package com.tvtracker.show.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SeasonResponse(
        UUID id, int seasonNumber, String name, boolean watched, Instant watchedAt, List<EpisodeResponse> episodes) {}
