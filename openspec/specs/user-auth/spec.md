# Auth Specification

## Purpose
Authentication and per-user account isolation for the application. Every other domain depends on this one — no data anywhere in the system is visible or writable across users.

## Requirements

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

### Requirement: Per-User Data Isolation
The system MUST ensure that no user can read or modify another user's shows, seasons, episodes, reviews, watch history, or favorites, under any circumstance. There are no social or shared-visibility features anywhere in the system.

#### Scenario: Cross-user access attempt
- GIVEN two distinct user accounts
- WHEN one user requests or modifies a resource owned by the other
- THEN the request is rejected regardless of whether the resource ID is guessable or otherwise valid
