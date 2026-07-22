## ADDED Requirements

### Requirement: Favorite Toggle on Show Detail
The UI SHALL provide a control on the show detail page header to add or remove the show from the user's favorites. The control MUST reflect the current favorite status from `GET /api/favorites/status`.

#### Scenario: User adds show to favorites from detail page
- **GIVEN** a show in the user's library that is not a favorite
- **WHEN** the user activates the favorite toggle on the show detail page
- **THEN** the show is added to favorites via the API
- **AND** the toggle reflects the favorited state without a full page reload

#### Scenario: User removes show from favorites from detail page
- **GIVEN** a show the user has marked as a favorite
- **WHEN** the user activates the favorite toggle on the show detail page
- **THEN** the show is removed from favorites via the API
- **AND** the toggle reflects the non-favorited state

### Requirement: Suggestion Badge on Show Detail
When a show qualifies as a suggestion and is not yet favorited, the show detail page SHALL display a visible suggestion badge distinct from the favorited state.

#### Scenario: Suggested show shows badge before favoriting
- **GIVEN** a show that appears in favorite suggestions and is not yet favorited
- **WHEN** the user views the show detail page
- **THEN** a suggestion badge is visible
- **AND** the show is not shown as favorited until the user opts in

#### Scenario: Badge hidden after favoriting
- **GIVEN** a suggested show on the show detail page
- **WHEN** the user adds it to favorites
- **THEN** the suggestion badge is no longer shown
- **AND** the favorited toggle state is shown instead

### Requirement: Suggestions Panel with Opt-In
The library page SHALL display a panel of suggested shows derived from the user's own reviews. Each suggestion MUST offer an explicit control to add the show to favorites. Suggestions MUST NOT be auto-added to favorites.

#### Scenario: User opts in from suggestions panel
- **GIVEN** a show listed in the suggestions panel
- **WHEN** the user clicks add to favorites on that row
- **THEN** the show is added to favorites via the API
- **AND** the show is removed from the suggestions panel

#### Scenario: Empty suggestions panel
- **GIVEN** the user has no qualifying suggestions
- **WHEN** they view the library page
- **THEN** the suggestions panel shows an empty or hidden state
- **AND** no shows are marked as favorites without explicit user action
