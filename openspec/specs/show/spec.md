# Show Specification

## Purpose
Discovering shows and managing a user's personal library of shows, seasons, and episodes.

Show metadata is sourced from [The Movie Database (TMDB)](https://www.themoviedb.org/) via its API. All integration MUST comply with the [TMDB API Terms of Use](https://www.themoviedb.org/api-terms-of-use) and [TMDB Terms of Use](https://www.themoviedb.org/terms-of-use).

## Requirements

### Requirement: TMDb API Compliance
The system SHALL integrate with TMDB in compliance with TMDB's API Terms of Use.

#### Scenario: Backend-only API access
- GIVEN any request for TMDB metadata
- WHEN the system fetches from TMDB
- THEN the request is made only from the backend
- AND the TMDB API key is never exposed to clients

#### Scenario: Attribution is displayed
- GIVEN the application displays TMDB-sourced metadata or images
- WHEN a user views an About or Credits section (or equivalent)
- THEN an approved TMDB logo is shown less prominently than the application's own branding
- AND the required disclaimer is displayed prominently: "This product uses the TMDB API but is not endorsed or certified by TMDB."

#### Scenario: Responsible API usage
- GIVEN the backend calls the TMDB API
- WHEN rate limits are hit or transient errors occur
- THEN the system applies backoff and retry rather than degrading TMDB service for other users
- AND the system identifies itself to TMDB (does not conceal the calling application)

#### Scenario: Caching and retention limits
- GIVEN metadata obtained from TMDB
- WHEN it is stored beyond the active request
- THEN it is stored only as part of a user's explicit library snapshot (not as a shared search cache)
- AND any non-library cached TMDB responses MUST NOT be retained longer than six months

#### Scenario: Prohibited uses
- GIVEN any use of TMDB content or APIs
- WHEN the system stores, processes, or displays that data
- THEN the system MUST NOT use TMDB content to train or validate machine-learning or AI systems
- AND the system MUST NOT use TMDB as a general-purpose image hosting service
- AND the system MUST NOT sublicense, sell, or derive direct commercial revenue from TMDB APIs or content without a separate written agreement with TMDB

### Requirement: Show Search
The system SHALL allow a user to search for shows by title, returning TMDB-sourced metadata, without persisting search results that are not added to the library.

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

### Requirement: Library Status
The system SHALL support a "Plan to Watch" status at the show level, in addition to having no explicit status.

#### Scenario: Marking a show as planned
- GIVEN a show in the user's library
- WHEN the user sets its status to "Plan to Watch"
- THEN that status is reflected when the user views their library

### Requirement: Remove Show from Library
The system SHALL allow a user to remove a show from their library. Removal MUST also remove that user's watch history, reviews, and favorite flags associated with that show, and MUST NOT affect any other user's data.

#### Scenario: Removing a show cleans up dependent data
- GIVEN a show in a user's library with existing reviews and watch history
- WHEN the user removes the show
- THEN the show and its dependent reviews, watch history, and favorite flags for that user are removed
- AND no other user's library is affected
