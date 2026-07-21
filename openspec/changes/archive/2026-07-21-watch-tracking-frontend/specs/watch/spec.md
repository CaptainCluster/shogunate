## ADDED Requirements

### Requirement: Watch Controls on Show Detail
The frontend SHALL provide separate mark-watched and unmark actions at the episode, season, and show level on the show detail page for shows in the authenticated user's library.

#### Scenario: Mark episode from show detail
- **WHEN** the user views show detail for a show in their library
- **AND** an episode is unwatched
- **THEN** a mark-watched action is available for that episode
- **AND** activating it marks the episode watched via the watch API

#### Scenario: Unmark episode without confirmation modal
- **WHEN** the user activates unmark on a watched episode from show detail
- **THEN** the unmark request is sent immediately without a confirmation step
- **AND** the episode becomes unwatched in the UI after the mutation succeeds

#### Scenario: Mark season or show cascades in UI
- **WHEN** the user marks a season or show watched from show detail
- **THEN** all affected seasons and episodes on the page reflect watched state after the mutation succeeds without a manual page refresh

### Requirement: Season and Show Unmark Confirmation Modal
The frontend MUST require explicit user confirmation before sending an unmark request for a watched season or show.

#### Scenario: Season unmark shows confirmation
- **WHEN** the user activates unmark on a watched season
- **THEN** a confirmation dialog is displayed describing that the season and its episodes will be unmarked
- **AND** no API request is sent until the user confirms

#### Scenario: Cancel confirmation preserves state
- **WHEN** the user cancels the season or show unmark confirmation dialog
- **THEN** no unmark API request is sent
- **AND** watched state on the page is unchanged

#### Scenario: Confirm sends request with confirm flag
- **WHEN** the user confirms unmark for a watched season or show
- **THEN** the frontend sends the unmark request with `confirm=true`
- **AND** all affected rows on show detail reflect unwatched state after success

### Requirement: Season Progress on Show Detail
The show detail page SHALL display derived episode watch progress for each season.

#### Scenario: Progress updates after marking an episode
- **WHEN** the user marks an episode watched from show detail
- **THEN** the season header shows an updated watched-episode count out of total episodes for that season

#### Scenario: Progress reflects full season after cascade mark
- **WHEN** the user marks a season watched
- **THEN** that season's progress shows all episodes watched

### Requirement: Watch Mutation Server State via TanStack Query
Watch mark and unmark API calls MUST be consumed through TanStack Query mutation hooks; pages and presentational components MUST NOT call the watch API directly.

#### Scenario: Show detail refreshes after watch mutation
- **WHEN** a watch or unmark mutation succeeds for any target on a show detail page
- **THEN** the show detail query is invalidated or refetched
- **AND** watch state for the show, seasons, and episodes updates from server data
