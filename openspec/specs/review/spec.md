# Review Specification

## Purpose
User reviews and ratings of episodes, seasons, and shows, kept independent at each level and private to their author.

## Requirements

### Requirement: Review Composition
A review SHALL consist of a numeric rating between 1 and 5 in increments of 0.5, and free-form written text. A review MAY be written at the episode, season, or show level.

#### Scenario: Creating a valid review
- GIVEN a user viewing an episode, season, or show
- WHEN they submit a rating of 3.5 and accompanying text
- THEN the review is saved at that level, associated with that user

#### Scenario: Rejecting an invalid rating
- GIVEN a user submitting a review
- WHEN the rating is not a multiple of 0.5 or falls outside 1–5
- THEN the review is rejected

### Requirement: Independence Between Levels
A review or rating at one level (episode, season, or show) SHALL NOT be derived from, averaged into, or otherwise affect a review or rating at another level.

#### Scenario: Show rating unaffected by episode ratings
- GIVEN a show with several distinctly-rated episodes
- WHEN the user views or sets the show's own rating
- THEN the show's rating is independent of any episode or season ratings

### Requirement: Review Privacy
A review SHALL be visible only to the user who wrote it. The system MUST NOT expose any user's reviews to any other user under any circumstance.

#### Scenario: Another user cannot view a review
- GIVEN a review written by User A
- WHEN User B requests it, directly or indirectly
- THEN the request is rejected or returns no data

### Requirement: Review Editing
A user SHALL be able to edit or delete their own review at any time.

#### Scenario: Editing an existing review
- GIVEN a review previously written by the user
- WHEN the user updates its rating or text
- THEN the stored review reflects the update
