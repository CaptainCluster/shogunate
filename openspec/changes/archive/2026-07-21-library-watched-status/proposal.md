# Proposal: Library WATCHED Status

## Why

The PRD defines three library statuses: None, Plan to Watch, and Watched (once fully watched). Only the first two are implemented today. Users cannot see at a glance which shows they have finished.

## What

Add a persisted `WATCHED` library status that is set automatically when every episode in a show is watched, and cleared when any episode is unwatched—restoring the user's prior status (`NONE` or `PLAN_TO_WATCH`).

## Scope

- Backend: DB migration, sync service wired into watch mark/unmark, PATCH validation
- Frontend: types, read-only WATCHED on detail page, library labels, cache invalidation
- Spec: delta update to show library status requirements

## Out of scope

- Library list episode progress bars
- Auto-sync when new catalog episodes are added after a show is WATCHED
