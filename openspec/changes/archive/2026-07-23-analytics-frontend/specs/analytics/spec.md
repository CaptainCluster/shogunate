## ADDED Requirements

### Requirement: Analytics Dashboard Route
The frontend SHALL provide a protected analytics dashboard at `/analytics` accessible only to authenticated users. The dashboard MUST consume the backend analytics REST endpoints and MUST NOT call analytics APIs from page components directly (TanStack Query hooks only).

#### Scenario: Authenticated user views analytics dashboard
- **GIVEN** a signed-in user with watch history
- **WHEN** the user navigates to `/analytics`
- **THEN** the dashboard loads and displays analytics sections sourced from `/api/analytics/*`
- **AND** no unauthenticated request is sent

#### Scenario: Unauthenticated user cannot access dashboard
- **GIVEN** a user who is not signed in
- **WHEN** the user attempts to navigate to `/analytics`
- **THEN** the user is redirected to the login flow

### Requirement: Totals Section
The analytics dashboard SHALL display all-time watched counts for episodes, seasons, and shows, sourced from `GET /api/analytics/totals`.

#### Scenario: Totals reflect user watch history
- **GIVEN** a user with WATCHED events in their watch history log
- **WHEN** the user views the totals section on the analytics dashboard
- **THEN** episode, season, and show counts are displayed

### Requirement: Watch Counts by Period Section
The analytics dashboard SHALL display watch counts grouped by target type for a selectable time period, sourced from `GET /api/analytics/watch-counts`. The section MUST allow the user to choose period type (MONTH, YEAR, or CUSTOM) and supply the required date parameters.

#### Scenario: User selects a calendar month
- **GIVEN** a user on the analytics dashboard
- **WHEN** the user selects period MONTH and a date within a month
- **THEN** the dashboard requests watch counts for that calendar month
- **AND** displays episode, season, and show counts for the resolved date range

#### Scenario: User selects a custom date range
- **GIVEN** a user on the analytics dashboard
- **WHEN** the user selects period CUSTOM and provides valid from and to dates
- **THEN** the dashboard requests watch counts for the inclusive custom range
- **AND** displays the resolved from and to dates echoed from the API response

### Requirement: Longest Time to Watch Section
The analytics dashboard SHALL display a ranked list of shows ordered by elapsed time between first and last watched episode, sourced from `GET /api/analytics/longest-to-watch`. Each row MUST link to the show detail page.

#### Scenario: Ranked list shows longest first
- **GIVEN** a user with multiple shows with watch history spans
- **WHEN** the user views the longest-to-watch section
- **THEN** shows are listed in descending duration order with formatted durations
- **AND** each show title links to `/library/{showId}`

### Requirement: Favorites Analytics Section
The analytics dashboard SHALL display the user's explicitly favorited shows, sourced from `GET /api/analytics/favorites`. Suggestion-only shows MUST NOT appear. Show titles MUST be resolved client-side from the user's library list.

#### Scenario: Only explicit favorites shown
- **GIVEN** a user has one show explicitly favorited and another show that appears only in suggestions
- **WHEN** the user views the favorites section on the analytics dashboard
- **THEN** only the explicitly favorited show is listed

#### Scenario: Empty favorites state
- **GIVEN** a user with no explicit favorites
- **WHEN** the user views the favorites section
- **THEN** an empty-state message is shown

### Requirement: Watch Streaks Section
The analytics dashboard SHALL display current and longest consecutive-day watch streaks, sourced from `GET /api/analytics/watch-streaks`.

#### Scenario: Streak stats displayed
- **GIVEN** a user with WATCHED events across multiple calendar days
- **WHEN** the user views the watch streaks section
- **THEN** current streak days and longest streak days are displayed
- **AND** date ranges are shown when streaks are non-zero

### Requirement: Library Completion Section
The analytics dashboard SHALL display overall and per-show episode completion percentages, sourced from `GET /api/analytics/library-completion`.

#### Scenario: Per-show completion displayed
- **GIVEN** a user with partially watched shows in their library
- **WHEN** the user views the library completion section
- **THEN** overall completion and per-show watched/total episode counts are displayed
- **AND** fully watched shows are visually distinguished

### Requirement: Plan to Watch Count Section
The analytics dashboard SHALL display the count of library shows flagged plan-to-watch, sourced from `GET /api/analytics/plan-to-watch-count`.

#### Scenario: Plan-to-watch count displayed
- **GIVEN** a user with shows flagged plan-to-watch in their library
- **WHEN** the user views the plan-to-watch section
- **THEN** the count of plan-to-watch shows is displayed

### Requirement: UTC Date Formatting
The analytics dashboard SHALL display all analytics dates and timestamps in UTC with an explicit UTC label. Dates MUST NOT be converted to the user's local timezone.

#### Scenario: Longest-to-watch dates shown in UTC
- **GIVEN** a show with first and last watched episode timestamps
- **WHEN** the user views the longest-to-watch section
- **THEN** first and last watched dates are formatted in UTC with a visible UTC indicator

### Requirement: Analytics Query Invalidation
Mutations that change analytics source data MUST invalidate all analytics TanStack Query keys on success, including watch mark/unmark, favorite add/remove, and library add/remove/status-change operations.

#### Scenario: Watch mutation refreshes analytics
- **GIVEN** a user viewing analytics data that has been loaded into the query cache
- **WHEN** the user marks an episode watched
- **THEN** analytics queries are invalidated
- **AND** the dashboard reflects updated counts without a manual page reload

### Requirement: Analytics Navigation
The application navigation SHALL include a link to `/analytics` for authenticated users. The home page SHALL link to the analytics dashboard for signed-in users.

#### Scenario: Nav link visible when authenticated
- **GIVEN** a signed-in user
- **WHEN** the user views the application header
- **THEN** an Analytics navigation link to `/analytics` is visible
