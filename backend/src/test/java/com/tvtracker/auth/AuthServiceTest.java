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
import com.tvtracker.common.exception.UnauthorizedException;
import com.tvtracker.common.exception.ValidationException;
import com.tvtracker.common.security.JwtTokenProvider;
import java.time.Instant;
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
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void registerCreatesUserWithNormalizedUsername() {
        when(userRepository.existsByUsernameIgnoreCase("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash");

        authService.register(new RegisterRequest("TestUser", "password123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("testuser", saved.getUsername());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsernameIgnoreCase("testuser")).thenReturn(true);

        assertThrows(
                ValidationException.class, () -> authService.register(new RegisterRequest("testuser", "password123")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = new User(UUID.randomUUID(), "testuser", "hash", Instant.now());
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(jwtTokenProvider.createToken(user.getId(), "testuser")).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest("TestUser", "password123"));

        assertEquals("jwt-token", response.token());
        assertEquals("testuser", response.username());
        verify(jwtTokenProvider).createToken(user.getId(), "testuser");
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = new User(UUID.randomUUID(), "testuser", "hash", Instant.now());
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest("testuser", "wrong")));
        verify(jwtTokenProvider, never()).createToken(any(), eq("testuser"));
    }
}
