package com.tvtracker.auth.dto;

import java.util.UUID;

public record UserResponse(UUID id, String email, boolean emailVerified) {}
