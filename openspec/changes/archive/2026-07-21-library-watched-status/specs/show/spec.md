## MODIFIED Requirements

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
