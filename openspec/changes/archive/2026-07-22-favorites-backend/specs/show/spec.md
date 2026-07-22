## ADDED Requirements

### Requirement: Favorite Cleanup on Library Removal
When a user removes a show from their library, the system MUST delete that user's favorite row for that show, if one exists.

#### Scenario: Favorite row deleted on library removal
- **GIVEN** a user has favorited a show in their library
- **WHEN** the user removes that show from their library
- **THEN** the user's favorite row for that show is deleted
- **AND** favorites for the same show belonging to other users are unchanged
