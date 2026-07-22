## ADDED Requirements

### Requirement: Dedicated Search Page
TVmaze show search and add-to-library MUST be available on a dedicated search page separate from the library list page.

#### Scenario: User searches from dedicated page
- **GIVEN** an authenticated user
- **WHEN** they navigate to the search page
- **THEN** they can search TVmaze and add shows to their library
- **AND** the library list page does not include the search form

#### Scenario: Empty library links to search
- **GIVEN** a user with no shows in their library
- **WHEN** they view the library page
- **THEN** they are directed to the search page to add shows

## MODIFIED Requirements

### Requirement: Library Removal Confirmation
When a user initiates remove-from-library for a show in the UI, the system MUST display a confirmation dialog before sending the removal request. The dialog MUST warn that reviews, watch history, watch state, and favorites for the show will be permanently deleted. Dismissing or cancelling the dialog MUST NOT send a removal request.

#### Scenario: Cancel leaves show in library
- **GIVEN** a user viewing a show in their library on the show detail page or library list
- **WHEN** they click remove from library and then cancel or dismiss the confirmation dialog
- **THEN** no removal API request is sent
- **AND** the show remains in their library

#### Scenario: Confirm proceeds with removal
- **GIVEN** a user viewing a show in their library on the show detail page or library list
- **WHEN** they click remove from library and confirm the dialog
- **THEN** the removal API request is sent
- **AND** the show is removed from their library along with their associated reviews, watch history, watch state, and favorite row for that show
