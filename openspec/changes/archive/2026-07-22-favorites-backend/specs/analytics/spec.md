## MODIFIED Requirements

### Requirement: Favorites Reporting
The system SHALL surface the user's favorite shows, consistent with the rules defined in the Favorite specification. Favorites reporting MUST include only shows the user has explicitly added; suggestion-only shows MUST NOT be included.

#### Scenario: Analytics favorites match user-chosen favorites
- **GIVEN** a user has one show explicitly favorited and another show that appears only in suggestions
- **WHEN** the user requests favorites analytics
- **THEN** only the explicitly favorited show is reported
