package com.tvtracker.auth;

import com.tvtracker.auth.dto.AuthResponse;
import com.tvtracker.auth.dto.EmailRequest;
import com.tvtracker.auth.dto.LoginRequest;
import com.tvtracker.auth.dto.MessageResponse;
import com.tvtracker.auth.dto.RegisterRequest;
import com.tvtracker.auth.dto.ResetPasswordRequest;
import com.tvtracker.auth.dto.TokenRequest;
import com.tvtracker.auth.dto.UserResponse;
import com.tvtracker.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return new MessageResponse("Registration successful. Check your email to verify your account.");
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with email and password")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with token")
    public MessageResponse verifyEmail(@Valid @RequestBody TokenRequest request) {
        authService.verifyEmail(request.token());
        return new MessageResponse("Email verified successfully");
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification")
    public MessageResponse resendVerification(@Valid @RequestBody EmailRequest request) {
        authService.resendVerification(request.email());
        return new MessageResponse("Verification email sent");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public MessageResponse forgotPassword(@Valid @RequestBody EmailRequest request) {
        authService.forgotPassword(request.email());
        return new MessageResponse("If an account exists, a reset email has been sent");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return new MessageResponse("Password reset successfully");
    }
}

@RestController
@RequestMapping("/api")
class MeController {

    private final AuthService authService;

    MeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    @ApiResponse(responseCode = "200", description = "Current user profile")
    public UserResponse me(@CurrentUser UUID userId) {
        return authService.getCurrentUser(userId);
    }
}
