package com.tvtracker.show.tvmaze;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TvmazeShowRef(int id, String name, String summary, TvmazeImage image, String premiered, String url) {}
