## REMOVED Requirements

### Requirement: Registration and Email Verification
**Reason**: Auth identity is username-based; no email verification step.
**Migration**: Users register with username + password and can log in immediately.

### Requirement: Password Reset
**Reason**: Password reset was email-token-based; out of scope for username-only MVP.
**Migration**: No password recovery flow; users must know their password to log in.

## MODIFIED Requirements

### Requirement: Login
The system SHALL authenticate a user by username and password and issue a session token on success.

#### Scenario: Valid credentials
- GIVEN a registered account with correct credentials
- WHEN the user submits login
- THEN a session token is issued

#### Scenario: Invalid credentials
- GIVEN incorrect password for an existing account
- WHEN the user submits login
- THEN login is rejected
- AND no session token is issued

## ADDED Requirements

### Requirement: Registration
The system SHALL allow a user to register with a unique username and password. The account MUST be usable for login immediately after successful registration.

#### Scenario: Successful registration
- GIVEN a valid, unused username and a password
- WHEN the user submits registration
- THEN an account is created
- AND the user can log in with those credentials

#### Scenario: Duplicate username
- GIVEN a username already registered
- WHEN another user attempts to register with the same username (case-insensitive)
- THEN registration is rejected
