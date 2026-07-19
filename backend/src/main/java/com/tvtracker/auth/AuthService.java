package com.tvtracker.auth;

import com.tvtracker.auth.dto.AuthResponse;
import com.tvtracker.auth.dto.LoginRequest;
import com.tvtracker.auth.dto.RegisterRequest;
import com.tvtracker.auth.dto.UserResponse;
import com.tvtracker.common.exception.UnauthorizedException;
import com.tvtracker.common.exception.ValidationException;
import com.tvtracker.common.security.JwtTokenProvider;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ValidationException("Username is already taken");
        }

        Instant now = Instant.now();
        User user = new User(UUID.randomUUID(), username, passwordEncoder.encode(request.password()), now);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        String username = normalizeUsername(request.username());
        User user = userRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        return toAuthResponse(user);
    }

    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("User not found"));
        return new UserResponse(user.getId(), user.getUsername());
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                jwtTokenProvider.createToken(user.getId(), user.getUsername()), user.getId(), user.getUsername());
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}
