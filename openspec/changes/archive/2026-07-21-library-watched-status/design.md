# Design: Library WATCHED Status

## Status model

`WATCHED` is persisted in `user_library.library_status`. It is not user-settable via PATCH — only watch/unwatch operations drive transitions.

When leaving `WATCHED`, the library status is always set to `NONE`.

## Sync trigger

`LibraryStatusSyncService.syncAfterWatchChange(userId, showId)` runs at the end of `WatchService.markWatched` and `unmarkWatched` within the same transaction.

## Fully watched check

Load all episode IDs for the show. Count episodes with `user_watch_state.watched = true` and `target_type = EPISODE`. Fully watched iff `watchedCount == totalCount && totalCount > 0`. Do not rely on show-level watch state rows.

## PATCH rules

- Reject `libraryStatus: WATCHED` with 400
- Reject any PATCH when entry is currently `WATCHED` (user must unwatch first)

## Migration

`V6__library_watched_status.sql`: extend check constraint to include `WATCHED`.
