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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final String TEST_USER = "testuser";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesUserWithNormalizedUsername() {
        when(userRepository.existsByUsernameIgnoreCase(TEST_USER)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash");

        authService.register(new RegisterRequest("TestUser", "password123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(TEST_USER, saved.getUsername());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsernameIgnoreCase(TEST_USER)).thenReturn(true);

        assertThrows(
                ValidationException.class, () -> authService.register(new RegisterRequest(TEST_USER, "password123")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(TEST_USER)
                .passwordHash("hash")
                .createdAt(Instant.now())
                .build();
        when(userRepository.findByUsernameIgnoreCase(TEST_USER)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(jwtTokenProvider.createToken(user.getId(), TEST_USER)).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest("TestUser", "password123"));

        assertEquals("jwt-token", response.token());
        assertEquals(TEST_USER, response.username());
        verify(jwtTokenProvider).createToken(user.getId(), TEST_USER);
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(TEST_USER)
                .passwordHash("hash")
                .createdAt(Instant.now())
                .build();
        when(userRepository.findByUsernameIgnoreCase(TEST_USER)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest(TEST_USER, "wrong")));
        verify(jwtTokenProvider, never()).createToken(any(), eq(TEST_USER));
    }
}
