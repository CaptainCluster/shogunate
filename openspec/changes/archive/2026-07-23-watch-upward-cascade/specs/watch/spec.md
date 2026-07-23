## ADDED Requirements

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

## MODIFIED Requirements

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
