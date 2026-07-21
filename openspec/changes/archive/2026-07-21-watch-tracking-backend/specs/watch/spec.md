## ADDED Requirements

### Requirement: Watch REST Endpoints
The system SHALL expose authenticated REST endpoints to mark and unmark watched state at the episode, season, and show level.

#### Scenario: Mark episode watched via API
- **WHEN** an authenticated user sends `POST /api/watch/episodes/{id}` for an episode in their library
- **THEN** the system returns success with no response body
- **AND** the episode's current watched state becomes true with a recorded timestamp

#### Scenario: Unmark season requires confirmation parameter
- **WHEN** an authenticated user sends `DELETE /api/watch/seasons/{id}` without `confirm=true`
- **THEN** the system rejects the request with a client error
- **AND** no watch state or history changes occur

#### Scenario: Unmark show with confirmation succeeds
- **WHEN** an authenticated user sends `DELETE /api/watch/shows/{id}?confirm=true` for a show in their library
- **THEN** the system returns success with no response body
- **AND** the show and all its seasons and episodes become unwatched

### Requirement: Library Membership Required for Watch Operations
The system MUST reject watch and unwatch operations when the target's containing show is not in the authenticated user's library.

#### Scenario: Watch rejected for show not in library
- **WHEN** an authenticated user attempts to mark an episode watched for a show they have not added to their library
- **THEN** the request is rejected
- **AND** no watch state or history changes occur

#### Scenario: User cannot watch another user's library content
- **WHEN** an authenticated user attempts to mark watched a catalog target belonging to a show in another user's library only
- **THEN** the request is rejected
- **AND** no data belonging to the other user is modified

### Requirement: Cascade Event Tagging
When a mark or unmark operation cascades to descendant targets, each resulting history log entry MUST record whether it was cascade-triggered and MUST reference the top-level event that initiated the cascade.

#### Scenario: Show mark tags descendant events
- **WHEN** a user marks a show watched and the cascade affects seasons and episodes
- **THEN** a history log entry exists for the show with cascade-triggered set to false
- **AND** each season and episode entry has cascade-triggered set to true
- **AND** each descendant entry references the show-level event as its cascade source

## MODIFIED Requirements

### Requirement: Immutable Watch History Log
Every watch and unwatch action — including those triggered by a cascade — SHALL be recorded in a history log entry that MUST NOT be modified or deleted during normal watch and unwatch operations. This log is the sole basis for time-based analytics; current watched state alone is not sufficient for historical reporting. Bulk deletion of a user's history log entries for a show hierarchy is permitted only when that user removes the show from their library.

#### Scenario: Cascade actions are individually logged
- **GIVEN** a show being marked watched, cascading to 3 seasons and 20 episodes
- **WHEN** the cascade completes
- **THEN** a history log entry exists for the show, each season, and each episode
- **AND** each entry is permanent and unalterable during subsequent watch and unwatch operations

#### Scenario: Normal watch operations do not delete history
- **GIVEN** existing history log entries for a user's watched targets
- **WHEN** the user marks or unmarks watched through the watch API
- **THEN** no existing history log entries are updated or deleted
