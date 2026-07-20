package com.tvtracker.show.tvmaze;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TvmazeSearchResult(TvmazeShowRef show) {}
