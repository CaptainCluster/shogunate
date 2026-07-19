package com.tvtracker.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendVerificationEmail(String email, String token) {
        log.info("Verification email for {}: use token {}", email, token);
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        log.info("Password reset email for {}: use token {}", email, token);
    }
}
