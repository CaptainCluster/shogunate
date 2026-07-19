## ADDED Requirements

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

### Requirement: Duplicate Library Add Rejection
The system SHALL reject attempts to add a show that is already present in the user's library (same external show identifier), returning a conflict response without modifying existing data.

#### Scenario: Duplicate add is rejected
- GIVEN a show already in the user's library for a given external show identifier
- WHEN the user attempts to add the same show again
- THEN the request is rejected with a conflict response
- AND the existing library entry is unchanged

## MODIFIED Requirements

### Requirement: Remove Show from Library
The system SHALL allow a user to remove a show from their library. Removal MUST also remove that user's watch history, reviews, and favorite flags associated with that show, and MUST NOT affect any other user's data. Removal MUST delete the show's seasons and episodes as part of the same operation.

#### Scenario: Removing a show cleans up dependent data
- GIVEN a show in a user's library with seasons and episodes (and any associated reviews, watch history, or favorite flags when those features exist)
- WHEN the user removes the show
- THEN the show, its seasons, its episodes, and dependent reviews, watch history, and favorite flags for that user are removed
- AND no other user's library is affected

#### Scenario: Duplicate add does not create partial data
- GIVEN a user attempts to add a show already in their library
- WHEN the duplicate add is rejected
- THEN no additional season or episode rows are created
