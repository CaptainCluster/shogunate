# Favorite Specification

## Purpose
Identifying a user's favorite shows and seasons, combining automatic suggestion with manual control.

## Requirements

### Requirement: Auto-Suggested Favorites
The system SHALL suggest favorite shows and seasons based on the user's own highest average ratings from their reviews.

#### Scenario: Highest-rated show is suggested
- GIVEN a user has rated several shows
- WHEN they view their favorites
- THEN the show(s) with the highest average rating are suggested as favorites

### Requirement: Manual Override
A user SHALL be able to manually flag or unflag any show or season as a favorite, independent of the auto-suggestion logic.

#### Scenario: Manually flagging a lower-rated show
- GIVEN a show that is not the highest-rated
- WHEN the user manually flags it as a favorite
- THEN it appears among the user's favorites regardless of its rating

#### Scenario: Manually unflagging an auto-suggested favorite
- GIVEN a show that is auto-suggested as a favorite due to its rating
- WHEN the user manually unflags it
- THEN it no longer appears among the user's favorites

### Requirement: Distinguishing Favorite Source
The system SHOULD indicate whether a given favorite is present due to auto-suggestion, a manual flag, or both, so the user can tell them apart.

#### Scenario: Both sources apply
- GIVEN a show that is both highest-rated and manually flagged
- WHEN the user views their favorites
- THEN the show is shown as a favorite with both sources indicated
