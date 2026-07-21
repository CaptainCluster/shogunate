## ADDED Requirements

### Requirement: Show Detail Includes Watch State
When a user requests show detail for a show in their library, the response SHALL include the user's current watched state and watched timestamp for the show, each season, and each episode.

#### Scenario: Unwatched episode shows default state
- **WHEN** an authenticated user requests show detail for a show in their library
- **AND** the user has not marked an episode watched
- **THEN** that episode is reported as unwatched with no watched timestamp

#### Scenario: Watched targets reflect current state
- **WHEN** an authenticated user requests show detail after marking a season watched
- **THEN** the season and its episodes are reported as watched with timestamps matching the mark operation

## MODIFIED Requirements

### Requirement: Remove Show from Library
The system SHALL allow a user to remove a show from their library. Removal MUST remove that user's library entry and that user's watch state, watch history log entries, reviews, and favorite flags associated with that show. Removal MUST NOT affect any other user's data. When no users remain linked to the show, the global catalog show and its seasons and episodes MUST be deleted.

#### Scenario: Removing a show cleans up user-scoped data
- **GIVEN** a show in a user's library with associated reviews, watch history log entries, watch state, or favorite flags
- **WHEN** the user removes the show
- **THEN** the user's library entry and all of that user's data for the show hierarchy are removed
- **AND** no other user's library or data is affected

#### Scenario: Orphan catalog is removed when last user leaves
- **GIVEN** a show with only one user's library entry
- **WHEN** that user removes the show
- **THEN** the global show, seasons, and episodes are deleted after user-scoped cleanup

#### Scenario: Catalog preserved when other users remain
- **GIVEN** a show with library entries for multiple users
- **WHEN** one user removes the show
- **THEN** the global catalog remains for users who still have the show in their library
