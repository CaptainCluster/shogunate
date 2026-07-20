package com.tvtracker.show.tvmaze;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TvmazeImage(String medium, String original) {}
