package com.tvtracker.auth;

public interface EmailService {

    void sendVerificationEmail(String email, String token);

    void sendPasswordResetEmail(String email, String token);
}
