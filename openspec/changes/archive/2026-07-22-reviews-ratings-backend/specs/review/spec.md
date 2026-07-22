## ADDED Requirements

### Requirement: Review REST Endpoints
The system SHALL expose authenticated REST endpoints to create, read, update, and delete reviews at the episode, season, and show level.

#### Scenario: Create review via API
- **WHEN** an authenticated user sends `POST /api/reviews` with a valid target, rating, and optional text for a target in their library
- **THEN** the system returns the created review with status 201
- **AND** `createdAt` is set
- **AND** `updatedAt` is null

#### Scenario: Get review by target
- **WHEN** an authenticated user sends `GET /api/reviews?targetType=&targetId=` for a target they have reviewed
- **THEN** the system returns their review for that target

#### Scenario: Get review when none exists
- **WHEN** an authenticated user sends `GET /api/reviews?targetType=&targetId=` for a target they have not reviewed
- **THEN** the system responds with not found

#### Scenario: Update review via API
- **WHEN** an authenticated user sends `PUT /api/reviews/{id}` with updated rating or text for their own review
- **THEN** the system returns the updated review
- **AND** `updatedAt` is populated

#### Scenario: Delete review via API
- **WHEN** an authenticated user sends `DELETE /api/reviews/{id}` for their own review
- **THEN** the system returns success with no response body
- **AND** the review no longer exists

#### Scenario: Duplicate review rejected
- **WHEN** an authenticated user sends `POST /api/reviews` for a target they have already reviewed
- **THEN** the request is rejected with a conflict response
- **AND** the existing review is unchanged

### Requirement: Library Membership Required for Review Operations
The system MUST reject review create, read, update, and delete operations when the target's containing show is not in the authenticated user's library.

#### Scenario: Review rejected for show not in library
- **WHEN** an authenticated user attempts to create or fetch a review for a target whose show is not in their library
- **THEN** the request is rejected
- **AND** no review data is created or returned

### Requirement: Rating Validation
The system MUST validate review ratings to be multiples of 0.5 in the inclusive range 1.0 through 5.0. Ratings below 1.0, above 5.0, or not on a 0.5 step MUST be rejected.

#### Scenario: Reject rating below minimum
- **WHEN** a user submits a review with rating 0.5
- **THEN** the review is rejected

#### Scenario: Reject invalid step
- **WHEN** a user submits a review with rating 3.25
- **THEN** the review is rejected

#### Scenario: Accept valid half-star rating
- **WHEN** a user submits a review with rating 3.5
- **THEN** the review is saved

### Requirement: Review Timestamps
A newly created review SHALL have a non-null `createdAt` and a null `updatedAt`. When a review is updated, `updatedAt` SHALL be set automatically by the database to the update time; the application MUST NOT set `updatedAt` on insert or update.

#### Scenario: New review has null updatedAt
- **WHEN** a user creates a review
- **THEN** the stored and returned review has `updatedAt` equal to null

#### Scenario: Edit sets updatedAt
- **WHEN** a user updates an existing review's rating or text
- **THEN** the stored and returned review has a non-null `updatedAt` reflecting the edit time
