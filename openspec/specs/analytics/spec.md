# Analytics Specification

## Purpose
Statistics derived from a user's own watch history and reviews. All analytics are computed per-user and never expose or incorporate another user's data.

## Requirements

### Requirement: Watch Counts by Period
The system SHALL report counts of episodes, seasons, and shows marked watched within a specified time period (e.g. a given month or year), derived from the watch history log rather than current watched state alone.

#### Scenario: Counting episodes watched in a month
- GIVEN a user's watch history log containing watched-episode entries across several months
- WHEN the user requests counts for a specific month
- THEN only entries with a timestamp inside that month are counted

### Requirement: Longest Time to Watch
For each show in a user's library, the system SHALL calculate the elapsed time between the timestamp of the first episode marked watched and the timestamp of the last episode marked watched, and SHALL support ranking shows by this duration.

#### Scenario: Ranking shows by time to watch
- GIVEN multiple shows with differing spans between first and last watched episode
- WHEN the user requests the longest-to-watch ranking
- THEN shows are ordered by that elapsed duration, longest first

### Requirement: Favorites Reporting
The system SHALL surface the user's favorite shows and seasons, consistent with the rules defined in the Favorite specification.

### Requirement: Per-User Scope
All analytics MUST be computed exclusively from the requesting user's own watch history, reviews, and favorites. No analytic may aggregate or compare data across users.

#### Scenario: Analytics never cross user boundaries
- GIVEN two users with independent watch histories
- WHEN either user requests their analytics
- THEN the result reflects only their own data, with no reference to the other user's activity
