# Watch Specification

## Purpose
Tracking watched state for episodes, seasons, and shows, including cascading behavior and the history log that powers analytics.

## Requirements

### Requirement: Mark Episode Watched
The system SHALL allow a user to mark a single episode as watched, recording the timestamp at which it was marked.

#### Scenario: Marking an episode watched
- GIVEN an unwatched episode
- WHEN the user marks it watched
- THEN the episode's watched state becomes true
- AND a watched timestamp is recorded

### Requirement: Mark Episode Watched Cascades Up
When all episodes in a season are watched, the system SHALL automatically mark that season as watched. When all episodes in a show are watched, the system SHALL automatically mark that show as watched. The parent `watchedAt` MUST equal the latest `watchedAt` among watched episodes in the completed scope (the most recently watched episode).

#### Scenario: Final episode marks season and show watched
- GIVEN a season with two episodes where the first is already watched
- WHEN the user marks the second episode watched
- THEN the season becomes watched
- AND the show becomes watched
- AND both parent `watchedAt` values equal the second episode's `watchedAt`

#### Scenario: Incomplete season does not promote parent
- GIVEN a season with two episodes where only one is watched
- WHEN the user marks that one episode watched
- THEN the season remains unwatched
- AND the show remains unwatched

#### Scenario: Single-episode season promotes on episode mark
- GIVEN a season with exactly one episode
- WHEN the user marks that episode watched
- THEN the season becomes watched with the episode's `watchedAt`
- AND the show becomes watched with the same `watchedAt`

### Requirement: Mark Season or Show Watched Cascades Down
Marking a season or show as watched SHALL mark all of its unwatched descendant episodes (and seasons, for a show) as watched, using the same timestamp as the top-level action. Already-watched descendants MUST retain their existing watched timestamp.

#### Scenario: Marking a show watched cascades to unwatched episodes
- GIVEN a show with unwatched seasons and episodes
- WHEN the user marks the show watched
- THEN every season and episode belonging to that show becomes watched
- AND each newly watched target shares the same watched timestamp as the show-level action

#### Scenario: Cascade mark preserves existing episode timestamps
- GIVEN a show where one episode is already marked watched with an earlier timestamp
- WHEN the user marks the show watched
- THEN the already-watched episode retains its original watched timestamp
- AND every previously unwatched episode becomes watched with the show-level action timestamp

### Requirement: Unmark Watched Requires Confirmation at Season/Show Level
The system SHALL allow a user to unmark any watched episode, season, or show. Unmarking a season or show MUST cascade to unmark all of its descendants, and MUST require explicit user confirmation before the action is performed.

#### Scenario: Unmark without confirmation is rejected
- GIVEN a watched season
- WHEN the user requests to unmark it without providing confirmation
- THEN the request is rejected
- AND no state changes

#### Scenario: Unmark with confirmation cascades down
- GIVEN a watched season with watched episodes
- WHEN the user requests to unmark it with confirmation provided
- THEN the season and all of its episodes become unwatched

### Requirement: Unmark Episode Cascades Up
When an episode is unmarked and the season or show is no longer fully watched (not every episode in scope is watched), the system SHALL automatically clear watched state on affected parent season and show rows.

#### Scenario: Unmarking one episode clears season and show
- GIVEN a fully watched show where every episode, season, and the show are marked watched
- WHEN the user unmarks one episode
- THEN that episode becomes unwatched
- AND the containing season becomes unwatched
- AND the show becomes unwatched

#### Scenario: Unmarking episode in incomplete season leaves siblings unchanged
- GIVEN a season where one of two episodes is watched and neither parent is watched
- WHEN the user unmarks the watched episode
- THEN both episodes are unwatched
- AND the season and show remain unwatched

### Requirement: No Rewatch Tracking
The system SHALL maintain exactly one current watched state and one current watched timestamp per episode, season, and show. Re-marking an already-watched target directly (not via a parent cascade) updates its existing timestamp rather than creating a distinct watch instance.

#### Scenario: Re-marking an already-watched episode directly
- GIVEN an episode already marked watched
- WHEN the user marks that same episode watched again
- THEN the episode still has a single current watched state
- AND its timestamp is updated to reflect the most recent action

### Requirement: Immutable Watch History Log
Every watch and unwatch action — including those triggered by a cascade — SHALL be recorded in a history log entry that MUST NOT be modified or deleted during normal watch and unwatch operations. This log is the sole basis for time-based analytics; current watched state alone is not sufficient for historical reporting. Bulk deletion of a user's history log entries for a show hierarchy is permitted only when that user removes the show from their library.

#### Scenario: Cascade actions are individually logged
- GIVEN a show being marked watched, cascading to 3 seasons and 20 episodes
- WHEN the cascade completes
- THEN a history log entry exists for the show, each season, and each episode
- AND each entry is permanent and unalterable during subsequent watch and unwatch operations

#### Scenario: Normal watch operations do not delete history
- GIVEN existing history log entries for a user's watched targets
- WHEN the user marks or unmarks watched through the watch API
- THEN no existing history log entries are updated or deleted

### Requirement: Watch REST Endpoints
The system SHALL expose authenticated REST endpoints to mark and unmark watched state at the episode, season, and show level.

#### Scenario: Mark episode watched via API
- GIVEN an episode in the user's library
- WHEN an authenticated user sends `POST /api/watch/episodes/{id}`
- THEN the system returns success with no response body
- AND the episode's current watched state becomes true with a recorded timestamp

#### Scenario: Unmark season requires confirmation parameter
- GIVEN a watched season in the user's library
- WHEN an authenticated user sends `DELETE /api/watch/seasons/{id}` without `confirm=true`
- THEN the system rejects the request with a client error
- AND no watch state or history changes occur

#### Scenario: Unmark show with confirmation succeeds
- GIVEN a watched show in the user's library
- WHEN an authenticated user sends `DELETE /api/watch/shows/{id}?confirm=true`
- THEN the system returns success with no response body
- AND the show and all its seasons and episodes become unwatched

### Requirement: Library Membership Required for Watch Operations
The system MUST reject watch and unwatch operations when the target's containing show is not in the authenticated user's library.

#### Scenario: Watch rejected for show not in library
- GIVEN an episode belonging to a show the user has not added to their library
- WHEN an authenticated user attempts to mark that episode watched
- THEN the request is rejected
- AND no watch state or history changes occur

#### Scenario: User cannot watch another user's library content
- GIVEN a catalog target belonging to a show in another user's library only
- WHEN an authenticated user attempts to mark it watched
- THEN the request is rejected
- AND no data belonging to the other user is modified

### Requirement: Cascade Event Tagging
When a mark or unmark operation cascades to other targets — whether downward to descendants or upward to ancestors — each resulting history log entry MUST record whether it was cascade-triggered and MUST reference the top-level event that initiated the cascade.

#### Scenario: Show mark tags descendant events
- GIVEN a user marks a show watched and the cascade affects seasons and episodes
- WHEN the cascade completes
- THEN a history log entry exists for the show with cascade-triggered set to false
- AND each season and episode entry has cascade-triggered set to true
- AND each descendant entry references the show-level event as its cascade source

#### Scenario: Episode mark tags upward promotion events
- GIVEN a user marks the final unwatched episode in a season
- WHEN the upward cascade promotes the season and show
- THEN a history log entry exists for the episode with cascade-triggered set to false
- AND season and show promotion entries have cascade-triggered set to true
- AND each promotion entry references the episode-level event as its cascade source

#### Scenario: Episode unmark tags upward demotion events
- GIVEN a fully watched show
- WHEN the user unmarks one episode
- THEN a history log entry exists for the episode unmark with cascade-triggered set to false
- AND season and show demotion entries have cascade-triggered set to true
- AND each demotion entry references the episode-level event as its cascade source

### Requirement: Watch Controls on Show Detail
The frontend SHALL provide separate mark-watched and unmark actions at the episode, season, and show level on the show detail page for shows in the authenticated user's library.

#### Scenario: Mark episode from show detail
- GIVEN the user views show detail for a show in their library
- AND an episode is unwatched
- WHEN the user activates mark-watched for that episode
- THEN the episode is marked watched via the watch API
- AND the episode becomes watched in the UI after the mutation succeeds

#### Scenario: Unmark episode without confirmation modal
- GIVEN the user activates unmark on a watched episode from show detail
- WHEN the unmark action completes
- THEN the unmark request is sent immediately without a confirmation step
- AND the episode becomes unwatched in the UI after the mutation succeeds

#### Scenario: Mark season or show cascades in UI
- GIVEN the user marks a season or show watched from show detail
- WHEN the mutation succeeds
- THEN all affected seasons and episodes on the page reflect watched state without a manual page refresh

### Requirement: Season and Show Unmark Confirmation Modal
The frontend MUST require explicit user confirmation before sending an unmark request for a watched season or show.

#### Scenario: Season unmark shows confirmation
- GIVEN the user activates unmark on a watched season
- WHEN the confirmation dialog is shown
- THEN it describes that the season and its episodes will be unmarked
- AND no API request is sent until the user confirms

#### Scenario: Cancel confirmation preserves state
- GIVEN the season or show unmark confirmation dialog is open
- WHEN the user cancels the dialog
- THEN no unmark API request is sent
- AND watched state on the page is unchanged

#### Scenario: Confirm sends request with confirm flag
- GIVEN the user confirms unmark for a watched season or show
- WHEN the unmark request completes
- THEN the frontend sends the unmark request with `confirm=true`
- AND all affected rows on show detail reflect unwatched state after success

### Requirement: Season Progress on Show Detail
The show detail page SHALL display derived episode watch progress for each season.

#### Scenario: Progress updates after marking an episode
- GIVEN the user marks an episode watched from show detail
- WHEN the UI updates
- THEN the season header shows an updated watched-episode count out of total episodes for that season

#### Scenario: Progress reflects full season after cascade mark
- GIVEN the user marks a season watched
- WHEN the UI updates
- THEN that season's progress shows all episodes watched

### Requirement: Watch Mutation Server State via TanStack Query
Watch mark and unmark API calls MUST be consumed through TanStack Query mutation hooks; pages and presentational components MUST NOT call the watch API directly.

#### Scenario: Show detail refreshes after watch mutation
- GIVEN a watch or unmark mutation succeeds for any target on a show detail page
- WHEN the show detail query is invalidated or refetched
- THEN watch state for the show, seasons, and episodes updates from server data
