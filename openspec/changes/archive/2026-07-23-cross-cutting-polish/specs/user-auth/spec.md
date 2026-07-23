## ADDED Requirements

### Requirement: Unauthenticated API Access Returns 401
Protected API endpoints SHALL return HTTP **401 Unauthorized** when the request has no valid JWT or an invalid/expired token. HTTP **403 Forbidden** SHALL be reserved for authenticated requests that fail an authorization check (e.g. show not in the caller's library).

#### Scenario: Missing token on protected endpoint
- **WHEN** a client calls a protected endpoint without an `Authorization` header
- **THEN** the response status is 401

#### Scenario: Invalid token on protected endpoint
- **WHEN** a client calls a protected endpoint with an invalid or expired JWT
- **THEN** the response status is 401

### Requirement: Session Cache Isolation on Logout and Login
The frontend MUST NOT display cached server data from a previous user session after logout or after a successful login as a different user. All TanStack Query cached server state SHALL be cleared on logout and before loading data for a newly authenticated session.

#### Scenario: Logout clears cached user data
- **WHEN** an authenticated user logs out
- **THEN** all TanStack Query cached server state is removed
- **AND** no library, review, favorite, or analytics data from that session remains in the client cache

#### Scenario: Login after another user does not show stale library
- **GIVEN** User A was logged in and had library data cached
- **WHEN** the client logs out User A and logs in as User B without a full page reload
- **AND** User B navigates to the library page
- **THEN** only User B's library data is displayed
- **AND** User A's cached library entries are not shown

#### Scenario: Login shows the new user's existing library
- **GIVEN** User A has show X in library (cached) and User B has show Y in library (not in A's library)
- **WHEN** the client logs out User A and logs in as User B without a full page reload
- **AND** User B navigates to the library page
- **THEN** show Y is displayed
- **AND** show X is not displayed
