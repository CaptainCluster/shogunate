## ADDED Requirements

### Requirement: Review Cleanup on Library Removal
When a user removes a show from their library, the system MUST delete all of that user's reviews whose targets belong to the removed show hierarchy (show, its seasons, and its episodes).

#### Scenario: Reviews deleted on library removal
- **GIVEN** a user has reviews on a show, one of its seasons, and one of its episodes
- **WHEN** the user removes that show from their library
- **THEN** all of that user's reviews for targets in that show hierarchy are deleted
- **AND** reviews for the same catalog targets belonging to other users are unchanged
