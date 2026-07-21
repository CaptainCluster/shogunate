package com.tvtracker.auth;

import com.tvtracker.auth.dto.AuthResponse;
import com.tvtracker.auth.dto.LoginRequest;
import com.tvtracker.auth.dto.MessageResponse;
import com.tvtracker.auth.dto.RegisterRequest;
import com.tvtracker.auth.dto.UserResponse;
import com.tvtracker.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return new MessageResponse("Registration successful. You can now log in.");
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with username and password")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class MeController {

    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    @ApiResponse(responseCode = "200", description = "Current user profile")
    public UserResponse me(@CurrentUser UUID userId) {
        return authService.getCurrentUser(userId);
    }
}
