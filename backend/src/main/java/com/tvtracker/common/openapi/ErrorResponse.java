package com.tvtracker.common.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Structured API error response")
public record ErrorResponse(
        @Schema(description = "Human-readable error message", example = "Show not found") String message,
        @Schema(description = "HTTP status code", example = "404") int status,
        @Schema(description = "ISO-8601 timestamp", example = "2026-07-23T12:00:00Z") String timestamp) {}
