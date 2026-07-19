package com.tvtracker.auth;

import com.tvtracker.auth.dto.AuthResponse;
import com.tvtracker.auth.dto.LoginRequest;
import com.tvtracker.auth.dto.RegisterRequest;
import com.tvtracker.auth.dto.ResetPasswordRequest;
import com.tvtracker.auth.dto.UserResponse;
import com.tvtracker.common.exception.UnauthorizedException;
import com.tvtracker.common.exception.ValidationException;
import com.tvtracker.common.security.JwtTokenProvider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final long verificationExpirationHours;
    private final long passwordResetExpirationHours;

    public AuthService(
            UserRepository userRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            @Value("${auth.verification-token-expiration-hours}") long verificationExpirationHours,
            @Value("${auth.password-reset-token-expiration-hours}") long passwordResetExpirationHours) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.verificationExpirationHours = verificationExpirationHours;
        this.passwordResetExpirationHours = passwordResetExpirationHours;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ValidationException("Email is already registered");
        }

        Instant now = Instant.now();
        User user = new User(UUID.randomUUID(), email, passwordEncoder.encode(request.password()), false, now);
        userRepository.save(user);
        issueVerificationToken(user, now);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email verification required");
        }

        return toAuthResponse(user);
    }

    @Transactional
    public void verifyEmail(String tokenValue) {
        Instant now = Instant.now();
        EmailVerificationToken token = emailVerificationTokenRepository
                .findByToken(tokenValue)
                .orElseThrow(() -> new ValidationException("Invalid verification token"));

        if (token.isUsed() || token.isExpired(now)) {
            throw new ValidationException("Invalid verification token");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        token.setUsedAt(now);
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository
                .findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new ValidationException("No account found for that email"));

        if (user.isEmailVerified()) {
            throw new ValidationException("Email is already verified");
        }

        issueVerificationToken(user, Instant.now());
    }

    @Transactional
    public void forgotPassword(String email) {
        userRepository
                .findByEmailIgnoreCase(normalizeEmail(email))
                .ifPresent(user -> issuePasswordResetToken(user, Instant.now()));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Instant now = Instant.now();
        PasswordResetToken token = passwordResetTokenRepository
                .findByToken(request.token())
                .orElseThrow(() -> new ValidationException("Invalid reset token"));

        if (token.isUsed() || token.isExpired(now)) {
            throw new ValidationException("Invalid reset token");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        token.setUsedAt(now);
    }

    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("User not found"));
        return new UserResponse(user.getId(), user.getEmail(), user.isEmailVerified());
    }

    private void issueVerificationToken(User user, Instant now) {
        String tokenValue = UUID.randomUUID().toString();
        EmailVerificationToken token = new EmailVerificationToken(
                UUID.randomUUID(),
                user,
                tokenValue,
                now.plus(verificationExpirationHours, ChronoUnit.HOURS),
                null,
                now);
        emailVerificationTokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), tokenValue);
    }

    private void issuePasswordResetToken(User user, Instant now) {
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken(
                UUID.randomUUID(),
                user,
                tokenValue,
                now.plus(passwordResetExpirationHours, ChronoUnit.HOURS),
                null,
                now);
        passwordResetTokenRepository.save(token);
        emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                jwtTokenProvider.createToken(user.getId(), user.getEmail()), user.getId(), user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
