package com.tvtracker.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvtracker.auth.dto.LoginRequest;
import com.tvtracker.auth.dto.RegisterRequest;
import com.tvtracker.auth.dto.ResetPasswordRequest;
import com.tvtracker.common.exception.UnauthorizedException;
import com.tvtracker.common.exception.ValidationException;
import com.tvtracker.common.security.JwtTokenProvider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                emailVerificationTokenRepository,
                passwordResetTokenRepository,
                emailService,
                passwordEncoder,
                jwtTokenProvider,
                24,
                1);
    }

    @Test
    void registerCreatesUnverifiedUserAndVerificationToken() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash");

        authService.register(new RegisterRequest("user@example.com", "password123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("user@example.com", saved.getEmail());
        assertEquals(false, saved.isEmailVerified());
        verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("user@example.com"), any());
    }

    @Test
    void loginRejectsUnverifiedUser() {
        User user = new User(UUID.randomUUID(), "user@example.com", "hash", false, Instant.now());
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(new LoginRequest("user@example.com", "password123")));
        verify(jwtTokenProvider, never()).createToken(any(), any());
    }

    @Test
    void verifyEmailMarksUserVerified() {
        User user = new User(UUID.randomUUID(), "user@example.com", "hash", false, Instant.now());
        EmailVerificationToken token = new EmailVerificationToken(
                UUID.randomUUID(), user, "token", Instant.now().plus(1, ChronoUnit.HOURS), null, Instant.now());
        when(emailVerificationTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        authService.verifyEmail("token");

        assertEquals(true, user.isEmailVerified());
        assert token.getUsedAt() != null;
    }

    @Test
    void resetPasswordRejectsUsedToken() {
        User user = new User(UUID.randomUUID(), "user@example.com", "hash", true, Instant.now());
        PasswordResetToken token = new PasswordResetToken(
                UUID.randomUUID(),
                user,
                "reset-token",
                Instant.now().plus(1, ChronoUnit.HOURS),
                Instant.now(),
                Instant.now());
        when(passwordResetTokenRepository.findByToken("reset-token")).thenReturn(Optional.of(token));

        assertThrows(
                ValidationException.class,
                () -> authService.resetPassword(new ResetPasswordRequest("reset-token", "newpassword1")));
        verify(userRepository, never()).save(any());
    }
}
