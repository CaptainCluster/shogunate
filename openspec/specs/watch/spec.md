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

### Requirement: Mark Season or Show Watched Cascades Down
Marking a season or show as watched SHALL mark all of its descendant episodes (and seasons, for a show) as watched, using the same timestamp as the top-level action.

#### Scenario: Marking a show watched cascades to all episodes
- GIVEN a show with unwatched seasons and episodes
- WHEN the user marks the show watched
- THEN every season and episode belonging to that show becomes watched
- AND all of them share the same watched timestamp as the show-level action

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

### Requirement: No Rewatch Tracking
The system SHALL maintain exactly one current watched state and one current watched timestamp per episode, season, and show. Re-marking an already-watched item as watched updates its existing state rather than creating a distinct watch instance.

#### Scenario: Re-marking an already-watched episode
- GIVEN an episode already marked watched
- WHEN the user marks it watched again
- THEN the episode still has a single current watched state
- AND its timestamp is updated to reflect the most recent action

### Requirement: Immutable Watch History Log
Every watch and unwatch action — including those triggered by a cascade — SHALL be recorded in a history log entry that MUST NOT be modified or deleted after creation. This log is the sole basis for time-based analytics; current watched state alone is not sufficient for historical reporting.

#### Scenario: Cascade actions are individually logged
- GIVEN a show being marked watched, cascading to 3 seasons and 20 episodes
- WHEN the cascade completes
- THEN a history log entry exists for the show, each season, and each episode
- AND each entry is permanent and unalterable going forward
