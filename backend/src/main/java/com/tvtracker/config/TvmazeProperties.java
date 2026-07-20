package com.tvtracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tvmaze")
public record TvmazeProperties(String baseUrl, String userAgent) {}
