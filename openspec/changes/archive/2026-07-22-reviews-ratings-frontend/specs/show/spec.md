## ADDED Requirements

### Requirement: Library Removal Confirmation
When a user initiates remove-from-library for a show in the UI, the system MUST display a confirmation dialog before sending the removal request. The dialog MUST warn that reviews, watch history, and watch state for the show will be permanently deleted. Dismissing or cancelling the dialog MUST NOT send a removal request.

#### Scenario: Cancel leaves show in library
- **GIVEN** a user viewing a show in their library on the show detail page or library list
- **WHEN** they click remove from library and then cancel or dismiss the confirmation dialog
- **THEN** no removal API request is sent
- **AND** the show remains in their library

#### Scenario: Confirm proceeds with removal
- **GIVEN** a user viewing a show in their library on the show detail page or library list
- **WHEN** they click remove from library and confirm the dialog
- **THEN** the removal API request is sent
- **AND** the show is removed from their library along with their associated reviews, watch history, and watch state for that show
