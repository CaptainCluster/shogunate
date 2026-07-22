# Show Specification

## Purpose
Discovering shows and managing a user's personal library of shows, seasons, and episodes.

Show metadata is sourced from [TVmaze](https://www.tvmaze.com/) via its API. All integration MUST comply with the [TVmaze API license (CC BY-SA 4.0)](https://www.tvmaze.com/api).

## Requirements

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

#### Scenario: Shared catalog persistence
- GIVEN metadata obtained from TVmaze
- WHEN a user explicitly adds a show to their library and no catalog entry exists for that TVmaze show
- THEN one global show, season, and episode tree is created keyed by TVmaze show identifier
- AND search results that were not added are not persisted

#### Scenario: Image hotlinking
- GIVEN poster images from TVmaze
- WHEN displayed in the application
- THEN images are hotlinked from TVmaze CDN URLs
- AND the system MUST NOT use TVmaze as a general-purpose image hosting service

### Requirement: Show Search
The system SHALL allow a user to search for shows by title, returning TVmaze-sourced metadata, without persisting search results that are not added to the library.

#### Scenario: Search returns results without side effects
- GIVEN a user submits a search query
- WHEN matching shows exist
- THEN matching show metadata is returned
- AND no records are created for shows that were not explicitly added

### Requirement: Shared Catalog Reuse
When a show already exists in the global catalog, adding it to a user's library SHALL reuse the existing catalog rows without creating duplicate show, season, or episode records and without re-fetching from TVmaze.

#### Scenario: Second user reuses existing catalog
- GIVEN a show already in the global catalog from a prior user's add
- WHEN another user adds the same TVmaze show to their library
- THEN a new user library entry links that user to the existing catalog show
- AND no duplicate show, season, or episode rows are created
- AND TVmaze is not called for that add

### Requirement: Duplicate Library Add Rejection
The system SHALL reject attempts to add a show that is already present in the user's library (same TVmaze show identifier), returning a conflict response without modifying existing data.

#### Scenario: Duplicate add is rejected
- GIVEN a show already in the user's library for a given TVmaze show identifier
- WHEN the user attempts to add the same show again
- THEN the request is rejected with a conflict response
- AND the existing library entry is unchanged

#### Scenario: Duplicate add does not create partial data
- GIVEN a user attempts to add a show already in their library
- WHEN the duplicate add is rejected
- THEN no additional library, season, or episode rows are created

### Requirement: Add Show to Library
The system SHALL allow a user to add a searched show to their personal library. If the show is not yet in the global catalog, the system SHALL fetch and store its full season and episode structure as it exists at the time of adding. If the show is already in the global catalog, the system SHALL link the user to the existing catalog entry.

#### Scenario: First add creates global catalog and library link
- GIVEN a show found via search that is not yet in the global catalog
- WHEN the user adds it to their library
- THEN a global show with all seasons and episodes is created from TVmaze metadata
- AND a user library entry links that user to the shared show

#### Scenario: Subsequent add reuses catalog
- GIVEN a show already in the global catalog
- WHEN a different user adds the same show to their library
- THEN only a user library entry is created for that user
- AND the existing global catalog is unchanged

### Requirement: Library Status
The system SHALL support library statuses at the show level for each user's library entry: no explicit status (`NONE`), `PLAN_TO_WATCH` (user-set), and `WATCHED` (set automatically when every episode is watched). `WATCHED` MUST NOT be settable via PATCH. When a show becomes fully watched, the system SHALL persist `WATCHED`. When any episode is unwatched while the show is `WATCHED`, the system SHALL set the library status to `NONE`. Shows with zero episodes MUST NOT enter `WATCHED`.

#### Scenario: Marking a show as planned
- GIVEN a show in the user's library
- WHEN the user sets its status to "Plan to Watch"
- THEN that status is reflected on the user's library entry when they view their library

#### Scenario: Fully watched show gets WATCHED
- GIVEN a show in the user's library with at least one episode
- AND not all episodes are watched
- WHEN the user marks the final unwatched episode as watched (or marks the show watched with cascade)
- THEN the library status becomes `WATCHED`

#### Scenario: Unwatching one episode reverts WATCHED to NONE
- GIVEN a show in the user's library with status `WATCHED`
- WHEN the user unmarks any episode as watched
- THEN the library status becomes `NONE`

#### Scenario: WATCHED is not patchable
- GIVEN a show in the user's library
- WHEN the user sends PATCH with `libraryStatus: WATCHED`
- THEN the request is rejected with a validation error

#### Scenario: Cannot PATCH intent while WATCHED
- GIVEN a show in the user's library with status `WATCHED`
- WHEN the user sends PATCH with `libraryStatus: NONE` or `PLAN_TO_WATCH`
- THEN the request is rejected with a validation error

#### Scenario: Zero episodes never WATCHED
- GIVEN a show in the user's library with zero episodes
- WHEN the user marks the show as watched
- THEN the library status remains `NONE` or `PLAN_TO_WATCH` (not `WATCHED`)

### Requirement: Orphan Catalog Cleanup
When a user removes a show from their library and no other users have that show in their library, the system SHALL delete the global catalog show and its seasons and episodes.

#### Scenario: Last user removal deletes catalog
- GIVEN a show in the global catalog with only one user's library entry
- WHEN that user removes the show from their library
- THEN the user's library entry and user-scoped data for that show are removed
- AND the global show, its seasons, and its episodes are deleted

#### Scenario: Partial removal preserves catalog for other users
- GIVEN a show in the global catalog with library entries for two users
- WHEN one user removes the show from their library
- THEN that user's library entry and user-scoped data are removed
- AND the global catalog rows remain available for the other user

### Requirement: Remove Show from Library
The system SHALL allow a user to remove a show from their library. Removal MUST remove that user's library entry and that user's watch state, watch history log entries, reviews, and favorite flags associated with that show. Removal MUST NOT affect any other user's data. When no users remain linked to the show, the global catalog show and its seasons and episodes MUST be deleted.

#### Scenario: Removing a show cleans up user-scoped data
- GIVEN a show in a user's library with associated reviews, watch history log entries, watch state, or favorite flags
- WHEN the user removes the show
- THEN the user's library entry and all of that user's data for the show hierarchy are removed
- AND no other user's library or data is affected

#### Scenario: Orphan catalog is removed when last user leaves
- GIVEN a show with only one user's library entry
- WHEN that user removes the show
- THEN the global show, seasons, and episodes are deleted after user-scoped cleanup

#### Scenario: Catalog preserved when other users remain
- GIVEN a show with library entries for multiple users
- WHEN one user removes the show
- THEN the global catalog remains for users who still have the show in their library

### Requirement: Review Cleanup on Library Removal
When a user removes a show from their library, the system MUST delete all of that user's reviews whose targets belong to the removed show hierarchy (show, its seasons, and its episodes).

#### Scenario: Reviews deleted on library removal
- **GIVEN** a user has reviews on a show, one of its seasons, and one of its episodes
- **WHEN** the user removes that show from their library
- **THEN** all of that user's reviews for targets in that show hierarchy are deleted
- **AND** reviews for the same catalog targets belonging to other users are unchanged

### Requirement: Show Detail Includes Watch State
When a user requests show detail for a show in their library, the response SHALL include the user's current watched state and watched timestamp for the show, each season, and each episode.

#### Scenario: Unwatched episode shows default state
- GIVEN an authenticated user requests show detail for a show in their library
- AND the user has not marked an episode watched
- WHEN the user views show detail
- THEN that episode is reported as unwatched with no watched timestamp

#### Scenario: Watched targets reflect current state
- GIVEN an authenticated user requests show detail after marking a season watched
- WHEN the user views show detail
- THEN the season and its episodes are reported as watched with timestamps matching the mark operation
