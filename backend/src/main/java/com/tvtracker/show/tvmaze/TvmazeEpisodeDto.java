package com.tvtracker.show.tvmaze;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TvmazeEpisodeDto(
        int id, String name, int season, Integer number, @JsonProperty("airdate") String airDate) {}
