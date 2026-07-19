# Product Requirements Document: Shogunate TV Show Tracker

## 1. Overview

A web + mobile (responsive/PWA) application that lets users track their TV show viewing history at three levels of granularity — **episode**, **season**, and **show** — with independent reviews at each level, and view analytics derived from watch timestamps.

Each user has a fully isolated environment. There are no social or sharing features of any kind — users cannot see each other, their libraries, or their reviews.

---

## 2. Goals

- Let users discover shows (via TMDb) and add them to a personal library.
- Let users mark episodes, seasons, and shows as watched/unwatched, with sensible cascading behavior.
- Let users write independent reviews (rating + text) at the episode, season, and show level.
- Surface analytics: favorite shows/seasons, watch counts over time periods, and time-to-watch metrics.
- Keep the system simple, personal, and self-contained (no social layer, no notifications, no analytics-driven growth metrics).

## 3. Non-Goals (Out of Scope for MVP)

- Social features (following, sharing, public profiles, comparing with friends).
- Notifications (email or in-app) of any kind.
- Rewatch tracking (multiple distinct watch-through instances per episode).
- Rating roll-up/auto-averaging between episode → season → show.
- Formal production infrastructure/hosting decisions (local/dev only for now).
- Formal product success metrics (DAU, retention, etc.) — this is a personal/portfolio project.

---

## 4. Users

Single user type: an **authenticated individual user**. Every user has their own isolated library, reviews, watch history, and favorites. No cross-user visibility exists anywhere in the product.

---

## 5. Functional Requirements

### 5.1 Authentication & Account Management
- Sign up / log in via **username + password**.
- Passwords stored using a strong one-way hash (e.g. bcrypt/Argon2) — never stored or logged in plaintext.
- Session/auth handled via a token-based mechanism (e.g. JWT), scoped per user.

### 5.2 Show Discovery & Library Management
- Users search TMDb directly from within the app.
- Search results can be added to the user's personal library.
- On adding a show, its metadata (title, overview, poster, air dates) and full season/episode structure is fetched from TMDb and stored locally in the app's database (a local snapshot, not a live pass-through), so the app functions correctly even if TMDb is later unreachable.
- Shows in a user's library can have a status of:
  - **None** (default — just added, no explicit status)
  - **Plan to Watch**
  - **Watched** (derived once fully watched — see 5.3)
- A show can be removed from a user's library (removes all associated watch/review/favorite data for that user only).

### 5.3 Watch Status Tracking
- Each **episode** has a watched state: `Unwatched` or `Watched`, with a `watchedAt` timestamp set when marked watched.
- Each **season** and **show** also carry their own watched state, settable directly with one click.
- **Cascading down (marking watched):** Marking a season watched marks all its episodes watched, using the same timestamp. Marking a show watched marks all its seasons and episodes watched, using the same timestamp.
- **Cascading down (unmarking watched):** Unmarking a season or show as watched also unmarks all of its episodes. This action requires **user confirmation** before proceeding, since it is destructive to fine-grained state.
- **No rewatch tracking:** each episode/season/show has exactly one current watched state and one current `watchedAt` timestamp at any time (i.e., re-marking as watched simply updates the timestamp — no separate watch instances are stored as "current state").
- **Full audit history is retained regardless:** every watch/unwatch action (including cascade-triggered ones) is recorded as an immutable event log entry with: user, target (episode/season/show), action type, timestamp, and whether it was cascade-triggered or direct. This log is what powers time-based analytics, independent of current state.

### 5.4 Reviews & Ratings
- A review can be written independently at the **episode**, **season**, or **show** level.
- A review consists of:
  - **Rating:** 1–5 stars, half-star precision (i.e. 0.5 increments: 1, 1.5, 2, ..., 5).
  - **Written text** (free-form).
- Ratings/reviews at different levels are **fully independent** — a show's rating is not calculated from its season or episode ratings, and vice versa. No auto-averaging or roll-up logic.
- A user can edit or delete their own reviews at any time.
- Reviews are **private** — visible only to the review's author. No other user, anonymous or authenticated, can view them.

### 5.5 Favorites
- Favorites apply at the **show** and **season** level.
- The system **auto-suggests** favorites based on highest average rating given by the user.
- The user can **manually flag/unflag** any show or season as a favorite, overriding the auto-suggestion.
- The UI should visually distinguish "auto-suggested favorite" from "manually marked favorite" if both mechanisms are active simultaneously.

### 5.6 Analytics & Statistics
All analytics are computed per-user, from the user's own watch-event history and reviews. Required metrics for MVP:

| Metric | Definition |
|---|---|
| Favorite shows/seasons | Per 5.5 — highest-rated and/or manually flagged |
| Watch counts by period | Count of episodes/seasons/shows marked watched within a given month/year/custom range, based on `watchedAt` timestamps from the event log |
| Longest time to watch a show | For each show, the elapsed time between the **timestamp of the first episode marked watched** and the **timestamp of the last episode marked watched**. Surfaced as a ranked list (longest → shortest) |
| Total watched counts | Total episodes / seasons / shows watched all-time |

- All time-based analytics must be derived from the immutable watch-event log (5.3), not from current-state timestamps alone, so that unmark/remark cycles don't corrupt historical stats.

---

## 6. Data Model (Conceptual)

- **User**: id, username, passwordHash, createdAt
- **Show**: id, userId, tmdbId, title, overview, posterUrl, firstAirDate, libraryStatus (`NONE` / `PLAN_TO_WATCH`), watched (bool), watchedAt, createdAt
- **Season**: id, showId, seasonNumber, name, watched (bool), watchedAt
- **Episode**: id, seasonId, episodeNumber, title, airDate, watched (bool), watchedAt
- **Review**: id, userId, targetType (`EPISODE`/`SEASON`/`SHOW`), targetId, rating (0.5–5.0, step 0.5), text, createdAt, updatedAt
- **WatchEvent** (immutable history log): id, userId, targetType, targetId, action (`WATCHED`/`UNWATCHED`), timestamp, triggeredByCascade (bool), cascadeSourceId (nullable — the parent action that caused this, if any)
- **Favorite**: id, userId, targetType (`SHOW`/`SEASON`), targetId, isManual (bool), createdAt

> Note: `Show`, `Season`, and `Episode` are stored as user-owned copies populated from TMDb at add-time (not shared/global rows across users), keeping each user's library fully isolated and independently mutable, at the cost of some data duplication. This can be revisited later if storage/consistency becomes a concern.

---

## 7. Technical Architecture

### 7.1 Stack
- **Backend:** Java, Spring Boot, Gradle
- **Frontend:** React, TypeScript, pnpm
- **Database:** PostgreSQL
- **External integration:** TMDb API for show/season/episode metadata and search

### 7.2 Monorepo Structure
Simple, independently-buildable folder layout — no monorepo orchestration tool (Nx/Turborepo):

```
/backend    → Spring Boot app, Gradle build
/frontend   → React + TypeScript app, pnpm
```

Each side builds and runs independently; no shared build graph or task runner ties them together at this stage.

### 7.3 API
- REST API exposed by the Spring Boot backend, consumed by the React frontend.
- All endpoints scoped to the authenticated user (via auth token) — no endpoint can return another user's data.

### 7.4 Hosting/Deployment
Out of scope for this PRD. MVP target is local/dev environment only; infrastructure and deployment decisions (containerization, cloud provider, CI/CD) will be addressed separately once the product is functional.

---

## 8. Non-Functional Requirements

### 8.1 Testing
- **Backend:** Unit tests for service/business logic (e.g. cascade behavior, analytics calculations) and integration tests for REST endpoints and database interactions.
- **Frontend:** Unit tests for components and utility/analytics-formatting logic, plus integration tests for key user flows (adding a show, marking watched, writing a review).
- Testing is expected as part of MVP delivery, not deferred to a later phase.

### 8.2 Security
- Passwords hashed (never stored in plaintext).
- Auth tokens required on all non-public endpoints.
- Strict per-user data isolation enforced at the query/service layer, not just the UI layer.

### 8.3 Data Integrity
- Cascading watch/unwatch actions must be atomic (a season's cascade to its episodes should not partially succeed).
- The watch-event log is append-only/immutable — no updates or deletes to historical entries, ensuring analytics remain accurate over time.

---

## 9. Open Assumptions to Confirm

These were reasonable defaults chosen to keep things unambiguous, flagged here in case they need adjustment:

1. `Plan to Watch` status applies only at the **show** level, not season/episode.
2. Show/season/episode metadata is **snapshotted locally** at add-time rather than kept live-synced with TMDb (so a TMDb update to episode counts, titles, etc. won't automatically propagate to a show already in a user's library).
3. Removing a show from a library deletes that user's associated watch/review/favorite data for that show, but has no effect on other users.

---

## 10. Future Considerations (Explicitly Deferred)

- Rewatch tracking as distinct events.
- Rating roll-up between hierarchy levels.
- Notifications (new episodes airing, etc.).
- Social features.
- Production hosting/CI-CD.
- Formal product success metrics.