## ADDED Requirements

### Requirement: Total Watched Counts
The system SHALL report all-time counts of episodes, seasons, and shows marked watched, grouped by target type, derived from the watch history log rather than current watched state alone.

#### Scenario: All-time episode count
- **GIVEN** a user's watch history log containing WATCHED entries across multiple months
- **WHEN** the user requests total watched counts
- **THEN** the episode count reflects all WATCHED episode log entries for that user with no date filter applied

### Requirement: Watch Streaks
The system SHALL report the user's current consecutive-day watch streak and longest-ever consecutive-day watch streak, derived from the watch history log. A streak day is any calendar day (UTC) with at least one WATCHED log entry.

#### Scenario: Longest streak across a gap
- **GIVEN** a user watched on consecutive days in March and again on consecutive days in July, with a gap in between
- **WHEN** the user requests watch streaks
- **THEN** the longest streak reflects the longer of the two runs, not the span including the gap

#### Scenario: Current streak ends after inactivity
- **GIVEN** a user's most recent WATCHED log entry occurred more than one calendar day before today (UTC)
- **WHEN** the user requests watch streaks
- **THEN** the current streak is reported as zero

### Requirement: Library Completion
The system SHALL report per-show and overall episode completion percentages for the user's library, based on current watched state: watched episodes divided by total catalog episodes per show.

#### Scenario: Partially watched show
- **GIVEN** a show in the user's library with 10 catalog episodes, 4 marked watched in current state
- **WHEN** the user requests library completion
- **THEN** that show's completion is reported as 40%

#### Scenario: Fully watched show
- **GIVEN** a show in the user's library with every episode marked watched in current state
- **WHEN** the user requests library completion
- **THEN** that show's completion is reported as 100% and marked fully watched

### Requirement: Plan to Watch Count
The system SHALL report the count of shows in the user's library flagged with plan-to-watch status.

#### Scenario: Count plan-to-watch shows only
- **GIVEN** a user has 3 shows flagged plan-to-watch, 5 with no status flag, and 2 fully watched (auto-synced status)
- **WHEN** the user requests plan-to-watch count
- **THEN** the count is 3
