# Show Specification

## Purpose
Discovering shows and managing a user's personal library of shows, seasons, and episodes.

## Requirements

### Requirement: Show Search
The system SHALL allow a user to search for shows by title, returning externally-sourced metadata, without persisting search results that are not added to the library.

#### Scenario: Search returns results without side effects
- GIVEN a user submits a search query
- WHEN matching shows exist
- THEN matching show metadata is returned
- AND no records are created for shows that were not explicitly added

### Requirement: Add Show to Library
The system SHALL allow a user to add a searched show to their personal library, capturing that show's full season and episode structure as it exists at the time of adding.

#### Scenario: Adding a show creates a full snapshot
- GIVEN a show found via search
- WHEN the user adds it to their library
- THEN the show, all of its seasons, and all of its episodes become part of that user's library
- AND this data belongs to that user only

### Requirement: Library Status
The system SHALL support a "Plan to Watch" status at the show level, in addition to having no explicit status.

#### Scenario: Marking a show as planned
- GIVEN a show in the user's library
- WHEN the user sets its status to "Plan to Watch"
- THEN that status is reflected when the user views their library

### Requirement: Remove Show from Library
The system SHALL allow a user to remove a show from their library. Removal MUST also remove that user's watch history, reviews, and favorite flags associated with that show, and MUST NOT affect any other user's data.

#### Scenario: Removing a show cleans up dependent data
- GIVEN a show in a user's library with existing reviews and watch history
- WHEN the user removes the show
- THEN the show and its dependent reviews, watch history, and favorite flags for that user are removed
- AND no other user's library is affected
