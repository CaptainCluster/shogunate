package com.tvtracker.show.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EpisodeResponse(
        UUID id, int episodeNumber, String title, LocalDate airDate, boolean watched, Instant watchedAt) {}
