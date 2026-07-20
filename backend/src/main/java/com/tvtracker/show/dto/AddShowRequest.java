package com.tvtracker.show.dto;

import jakarta.validation.constraints.NotNull;

public record AddShowRequest(@NotNull Integer tvmazeId) {}
