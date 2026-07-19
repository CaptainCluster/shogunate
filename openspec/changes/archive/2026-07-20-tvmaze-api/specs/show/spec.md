## REMOVED Requirements

### Requirement: TMDb API Compliance
**Reason**: External metadata provider switched from TMDB to TVmaze.
**Migration**: Replaced by "TVmaze API Compliance" requirement with CC BY-SA attribution rules.

## ADDED Requirements

### Requirement: TVmaze API Compliance
The system SHALL integrate with TVmaze in compliance with the [TVmaze API license (CC BY-SA 4.0)](https://www.tvmaze.com/api).

#### Scenario: Backend-only API access
- GIVEN any request for TVmaze metadata
- WHEN the system fetches from TVmaze
- THEN the request is made only from the backend
- AND the frontend never calls TVmaze directly

#### Scenario: Attribution is displayed
- GIVEN the application displays TVmaze-sourced metadata or images
- WHEN a user views an About or Credits section (or equivalent)
- THEN TVmaze is credited as the data source with a link to https://www.tvmaze.com
- AND the CC BY-SA license is noted

#### Scenario: Per-show attribution where metadata is shown
- GIVEN a show detail page displays TVmaze-sourced metadata
- WHEN the user views that page
- THEN a link to the show's TVmaze page (from the API `url` field) is available

#### Scenario: Responsible API usage
- GIVEN the backend calls the TVmaze API
- WHEN rate limits are hit or transient errors occur
- THEN the system applies backoff and retry rather than degrading TVmaze service for other users
- AND the system identifies itself via a descriptive User-Agent header

#### Scenario: Snapshot-only persistence
- GIVEN metadata obtained from TVmaze
- WHEN it is stored beyond the active request
- THEN it is stored only as part of a user's explicit library snapshot (not as a shared search cache)

#### Scenario: Image hotlinking
- GIVEN poster images from TVmaze
- WHEN displayed in the application
- THEN images are hotlinked from TVmaze CDN URLs
- AND the system MUST NOT use TVmaze as a general-purpose image hosting service

## MODIFIED Requirements

### Requirement: Show Search
The system SHALL allow a user to search for shows by title, returning TVmaze-sourced metadata, without persisting search results that are not added to the library.

#### Scenario: Search returns results without side effects
- GIVEN a user submits a search query
- WHEN matching shows exist
- THEN matching show metadata is returned
- AND no records are created for shows that were not explicitly added

### Requirement: Add Show to Library
The system SHALL allow a user to add a searched show to their personal library, capturing that show's full season and episode structure as it exists at the time of adding.

#### Scenario: Adding a show creates a full snapshot
- GIVEN a show found via search
- WHEN the user adds it to their library
- THEN the show, all of its seasons, and all of its episodes become part of that user's library
- AND this data belongs to that user only
