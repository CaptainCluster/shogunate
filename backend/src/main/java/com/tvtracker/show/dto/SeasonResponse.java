package com.tvtracker.show.dto;

import java.util.List;
import java.util.UUID;

public record SeasonResponse(UUID id, int seasonNumber, String name, List<EpisodeResponse> episodes) {}
