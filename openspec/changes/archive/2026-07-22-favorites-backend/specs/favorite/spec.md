## REMOVED Requirements

### Requirement: Auto-Suggested Favorites
**Reason**: Auto-suggestions are informational only; they no longer appear in the favorites list until the user explicitly adds a show.
**Migration**: Use `GET /api/favorites/suggestions` for rating-based recommendations; use `POST /api/favorites` to add a show to favorites.

### Requirement: Manual Override
**Reason**: Replaced by explicit show-only favorites and a separate suggestions API. There is no auto-inclusion to override.
**Migration**: Users add favorites via `POST /api/favorites` and remove via `DELETE /api/favorites/{showId}`.

### Requirement: Distinguishing Favorite Source
**Reason**: Favorites and suggestions are separate API surfaces; favorites are always user-chosen. Source distinction applies between favorites list and suggestions list, not within a merged favorites list.
**Migration**: Frontend compares `GET /api/favorites` with `GET /api/favorites/suggestions` or uses `GET /api/favorites/status?showId=`.

## ADDED Requirements

### Requirement: Show-Only Favorites
The system SHALL allow a user to mark shows as favorites. Only shows MAY be favorited; seasons and episodes MUST NOT be favoritable targets.

#### Scenario: User favorites a show
- **GIVEN** a show in the user's library that is not yet a favorite
- **WHEN** the user adds it to favorites
- **THEN** the show appears in the user's favorites list
- **AND** a persistent favorite row is stored for that user and show

#### Scenario: User removes a show from favorites
- **GIVEN** a show the user has marked as a favorite
- **WHEN** the user removes it from favorites
- **THEN** the show no longer appears in the user's favorites list
- **AND** the favorite row is deleted

### Requirement: Opt-In Favorites
A show MUST NOT appear in the user's favorites list unless the user has explicitly added it. Computed suggestions MUST NOT be treated as favorites.

#### Scenario: Suggested show is not a favorite until added
- **GIVEN** a show that qualifies as a suggestion based on the user's reviews
- **WHEN** the user requests their favorites list
- **THEN** that show is not included unless the user has explicitly added it

### Requirement: Favorite Suggestions
The system SHALL expose show suggestions derived from the user's own SHOW and SEASON review ratings. Season-level reviews MUST map to the parent show when computing suggestions. A show-level review MUST contribute its rating at full weight. A season-level review on a show with N seasons MUST contribute `rating / N` toward that show's score (one Nth the impact of a show-level review). Each show's score MUST be the maximum contribution among its show-level and season-level reviews. Suggestions MUST be returned separately from the favorites list and MUST contain show IDs only.

#### Scenario: Season review weighted by season count
- **GIVEN** a show with 5 seasons and a season review of 5.0, and no show-level review
- **WHEN** suggestions are computed for that show
- **THEN** the show's score is 1.0 (5.0 divided by 5)

#### Scenario: Show review outweighs season review on same show
- **GIVEN** a show with 5 seasons, a show review of 3.0, and a season review of 5.0
- **WHEN** suggestions are computed for that show
- **THEN** the show's score is 3.0 (maximum of 3.0 and 1.0)

#### Scenario: Season review on eight-season show
- **GIVEN** a show with 8 seasons and a season review of 4.0, and no show-level review
- **WHEN** suggestions are computed for that show
- **THEN** the show's score is 0.5 (4.0 divided by 8)

#### Scenario: Single-season show season review has full impact
- **GIVEN** a show with 1 season and a season review of 5.0, and no show-level review
- **WHEN** suggestions are computed for that show
- **THEN** the show's score is 5.0 (5.0 divided by 1)

#### Scenario: Global max score with ties
- **GIVEN** multiple shows whose weighted scores equal the user's global maximum
- **WHEN** the user requests favorite suggestions
- **THEN** all tied shows are returned as suggestions

#### Scenario: Favorited show excluded from suggestions
- **GIVEN** a show that qualifies as a suggestion and is already in the user's favorites
- **WHEN** the user requests favorite suggestions
- **THEN** that show is not included in the suggestions list

#### Scenario: After adding a favorite it leaves suggestions
- **GIVEN** a show appearing in the suggestions list
- **WHEN** the user adds that show to favorites
- **THEN** the show appears in the favorites list
- **AND** the show no longer appears in the suggestions list

### Requirement: Per-User Suggestion Scope
Favorite suggestions MUST be computed exclusively from the requesting user's own reviews. No suggestion may incorporate another user's ratings or favorites.

#### Scenario: Another user's ratings do not affect suggestions
- **GIVEN** two users who share the same catalog show but have different review ratings
- **WHEN** each user requests favorite suggestions
- **THEN** each user's suggestions reflect only their own weighted review scores

### Requirement: Favorite Cleanup on Library Removal
When a user removes a show from their library, the system MUST delete that user's favorite row for that show, if one exists.

#### Scenario: Favorite deleted on library removal
- **GIVEN** a user has favorited a show in their library
- **WHEN** the user removes that show from their library
- **THEN** the user's favorite row for that show is deleted
- **AND** other users' favorites for the same show are unchanged
