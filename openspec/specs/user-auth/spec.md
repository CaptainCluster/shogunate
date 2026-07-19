# Auth Specification

## Purpose
Authentication and per-user account isolation for the application. Every other domain depends on this one — no data anywhere in the system is visible or writable across users.

## Requirements

### Requirement: Registration and Email Verification
The system SHALL allow a user to register with an email and password, and MUST require email verification before that account can log in.

#### Scenario: Successful registration
- GIVEN a valid, unused email and a password
- WHEN the user submits registration
- THEN an unverified account is created
- AND a verification token is issued

#### Scenario: Login blocked before verification
- GIVEN a registered but unverified account
- WHEN the user attempts to log in with correct credentials
- THEN login is rejected
- AND the response indicates email verification is required

#### Scenario: Verification completes the account
- GIVEN a valid, unexpired verification token
- WHEN the user redeems it
- THEN the account becomes verified
- AND the user can subsequently log in

### Requirement: Login
The system SHALL authenticate a verified user by email and password and issue a session token on success.

#### Scenario: Valid credentials
- GIVEN a verified account with correct credentials
- WHEN the user submits login
- THEN a session token is issued

#### Scenario: Invalid credentials
- GIVEN incorrect password for an existing account
- WHEN the user submits login
- THEN login is rejected
- AND no session token is issued

### Requirement: Password Reset
The system SHALL allow a user to request a password reset and to set a new password by redeeming a time-limited reset token.

#### Scenario: Reset request
- GIVEN a registered email
- WHEN the user requests a password reset
- THEN a time-limited reset token is issued

#### Scenario: Valid reset
- GIVEN a valid, unexpired reset token
- WHEN the user submits a new password with that token
- THEN the password is updated
- AND the token cannot be redeemed again

#### Scenario: Expired or reused token
- GIVEN an expired or already-redeemed reset token
- WHEN the user attempts to redeem it
- THEN the request is rejected
- AND the password is not changed

### Requirement: Per-User Data Isolation
The system MUST ensure that no user can read or modify another user's shows, seasons, episodes, reviews, watch history, or favorites, under any circumstance. There are no social or shared-visibility features anywhere in the system.

#### Scenario: Cross-user access attempt
- GIVEN two distinct user accounts
- WHEN one user requests or modifies a resource owned by the other
- THEN the request is rejected regardless of whether the resource ID is guessable or otherwise valid
