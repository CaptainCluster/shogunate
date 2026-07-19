## ADDED Requirements

### Requirement: Duplicate Library Add Rejection
The system SHALL reject attempts to add a show that is already present in the user's library (same external show identifier), returning a conflict response without modifying existing data.

#### Scenario: Duplicate add is rejected
- GIVEN a show already in the user's library for a given TVmaze show identifier
- WHEN the user attempts to add the same show again
- THEN the request is rejected with a conflict response
- AND the existing library entry is unchanged

## MODIFIED Requirements

### Requirement: Remove Show from Library
The system SHALL allow a user to remove a show from their library. Removal MUST also remove that user's watch history, reviews, and favorite flags associated with that show, and MUST NOT affect any other user's data. Removal MUST delete the show's seasons and episodes as part of the same operation.

#### Scenario: Removing a show cleans up dependent data
- GIVEN a show in a user's library with seasons and episodes (and any associated reviews, watch history, or favorite flags when those features exist)
- WHEN the user removes the show
- THEN the show, its seasons, its episodes, and dependent reviews, watch history, and favorite flags for that user are removed
- AND no other user's library is affected

#### Scenario: Duplicate add does not create partial data
- GIVEN a user attempts to add a show already in their library
- WHEN the duplicate add is rejected
- THEN no additional season or episode rows are created
