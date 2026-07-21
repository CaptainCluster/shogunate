## 1. API & Types

- [x] 1.1 Extend `Episode`, `Season`, and `ShowDetail` in `frontend/src/api/showApi.ts` with `watched` and `watchedAt`
- [x] 1.2 Create `frontend/src/api/watchApi.ts` with mark/unmark functions for episodes, seasons, and shows (season/show unmark accept `confirm`)

## 2. Confirmation UX

- [x] 2.1 Create `frontend/src/components/ConfirmDialog.tsx` (accessible modal: dialog role, labelled title, Escape to cancel)
- [x] 2.2 Create `frontend/src/hooks/useConfirm.tsx` with `ConfirmProvider` and `confirm()` returning `Promise<boolean>`
- [x] 2.3 Wire `ConfirmProvider` in `App.tsx` or `Layout.tsx`

## 3. Watch Feature Module

- [x] 3.1 Create `frontend/src/features/watch/watchKeys.ts`
- [x] 3.2 Create `frontend/src/features/watch/hooks/useWatchMutations.ts` — TanStack mutations wrapping `watchApi`; invalidate `showKeys.detail(showId)` on success
- [x] 3.3 Create `frontend/src/features/watch/components/WatchButtonPair.tsx` — separate Mark / Unmark buttons; season/show unmark gated by `useConfirm`
- [x] 3.4 Create `frontend/src/features/watch/components/SeasonProgress.tsx` — derived `watchedCount/totalCount` from episode data
- [x] 3.5 Add `frontend/src/features/watch/watch.css` — watched row styling, button groups

## 4. Show Detail Integration

- [x] 4.1 Integrate show-level `WatchButtonPair` into `ShowDetailPage.tsx` header
- [x] 4.2 Integrate season-level `SeasonProgress` + `WatchButtonPair` into each season block
- [x] 4.3 Integrate episode-level `WatchButtonPair` on each episode row
- [x] 4.4 Surface mutation errors inline via `getErrorMessage`; disable buttons while pending

## 5. Vitest Setup & Tests

- [x] 5.1 Add Vitest, jsdom, and Testing Library dev dependencies; configure `vite.config.ts` and test setup file
- [x] 5.2 Add `pnpm test` and `pnpm test:run` scripts to `package.json`
- [x] 5.3 Test `useConfirm` — confirm resolves true, cancel/Escape resolves false
- [x] 5.4 Test `useWatchMutations` — correct API called; `showKeys.detail(showId)` invalidated on success
- [x] 5.5 Test `WatchButtonPair` — episode unmark fires immediately; season unmark waits for confirm

## 6. Verification & Close-out

- [x] 6.1 Run `pnpm lint`, `pnpm build`, and `pnpm test:run`
- [x] 6.2 Manual smoke: mark episode → season progress updates; mark season/show cascade; unmark season/show with confirm/cancel; episode unmark without modal
- [x] 6.3 Archive change and check off Phase 3.5 in `openspec/TASKS.md`
