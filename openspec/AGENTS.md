# AGENTS.md

Instructions for AI coding agents (e.g. Claude Code) working in this repository. Humans are welcome to read this too, but its primary audience is an agent about to make changes here.

This repo uses **OpenSpec** to drive spec-driven development. `openspec/specs/` is the live source of truth for system behavior — read it before starting any work. `docs/PRD.md`, `docs/ARCHITECTURE.md`, and `docs/TASKS.md` are the original human-authored planning documents that OpenSpec's specs were derived from; they remain useful as a readable overview and for implementation-level detail (package structure, DB schema, sequence flows) that intentionally does *not* live in `openspec/specs/` — but for current behavior, `openspec/specs/` wins if the two ever disagree.

**Workflow — use OpenSpec's commands, don't freehand it:**
- Use `openspec/TASKS.md` for phase-level progress and sequencing (what to build next).
- Start new work with `/opsx:propose` (or `/opsx:new`) to create a change under `openspec/changes/<change-name>/`.
- Implement via `/opsx:apply`, working through that change's own `tasks.md`.
- Verify with `/opsx:verify` where applicable, then complete with `/opsx:archive`, which merges the change's delta specs into `openspec/specs/` and moves the change folder to `openspec/changes/archive/`.
- Delta specs (`openspec/changes/<name>/specs/<domain>/spec.md`) describe ADDED/MODIFIED/REMOVED requirements relative to the current spec in `openspec/specs/<domain>/spec.md` — write these, don't restate the whole spec.
- When archiving a change that completes phase task(s), check the corresponding boxes in `openspec/TASKS.md` in the same commit.
- `docs/TASKS.md` is a historical copy of the original plan and is not kept in sync — use `openspec/TASKS.md` instead.

Don't improvise scope or structure that contradicts `openspec/specs/` or the documents in `docs/` — if something seems missing or contradictory between them, flag it instead of guessing (see "When Something Is Ambiguous" below).

---

## 1. Repository Layout

```
/openspec       → OpenSpec workspace: specs/ (live source of truth), TASKS.md (live phase roadmap), changes/ (work in progress + archive)
/docs           → PRD.md, ARCHITECTURE.md (implementation detail), TASKS.md (historical reference, not synced)
/backend        → Spring Boot + Gradle (Java)
/frontend       → React + TypeScript + pnpm
/scripts        → local dev helpers (e.g. test data seeding)
docker-compose.yml → local Postgres only, for local dev
```

`/backend` and `/frontend` build and run independently. There is no root-level build orchestration (no Nx/Turborepo) — run commands from within each directory.

---

## 2. Setup & Common Commands

### Local database
```
docker-compose up -d      # starts Postgres for local dev
```

### Local test data
After Postgres is running, seed a verified dev account (idempotent; safe to re-run):

```
./scripts/populate_test_data.sh
```

Default credentials:

- Username: `firstmock_lastmock`
- Password: `TestPassword123!`

The script uses the auth API when the backend is up; otherwise it inserts directly into Postgres. Extend `scripts/populate_test_data.sh` when new domains need local fixtures (shows, watch history, etc.).

### Backend (`/backend`)
```
./gradlew build           # build + run tests
./gradlew test             # tests only
./gradlew bootRun          # run the app locally
./gradlew spotlessApply    # auto-format code
./gradlew checkstyleMain checkstyleTest   # lint check
```
Run `spotlessApply` before finishing any backend change. Run `checkstyleMain`/`checkstyleTest` and resolve violations before considering a task done — don't leave lint failures for later.

### Frontend (`/frontend`)
```
pnpm install
pnpm dev                  # local dev server
pnpm build                # production build
pnpm test                 # unit/integration tests (Vitest)
pnpm lint                 # ESLint
pnpm format               # Prettier
```
Run `pnpm lint` and `pnpm format` before finishing any frontend change, same rule as backend.

---

## 3. Code Conventions

### Backend — feature-based packages
Each feature (`auth/`, `show/`, `watch/`, `review/`, `favorite/`, `analytics/`) is a full vertical slice: its own controller, service, repository, entity, and DTOs. **Do not** split new code across layered top-level folders (no top-level `controllers/`, `services/`, etc.) — a new feature gets its own package containing everything it needs. Shared code only goes in `common/` or `config/` (see `ARCHITECTURE.md` §2.1 for the exact layout).

### Frontend — mirror the backend features
`frontend/src/features/<feature>/` mirrors the backend feature it talks to. API calls live in `frontend/src/api/<feature>Api.ts`. Server state is managed exclusively through TanStack Query — do not introduce Redux, Zustand, or manual fetch+useState for anything that is server data. Local/UI-only state (form fields, modal visibility) uses plain `useState`.

Query key convention: namespace by feature and target, e.g. `['shows', userId]`, `['watch-status', targetType, targetId]`. When a mutation can trigger a cascade (see below), invalidate every query key that could be affected — not just the one directly mutated.

---

## 4. Non-Negotiable Constraints

These encode rules from the PRD/Architecture that are easy to accidentally violate. Do not deviate from these without explicit human confirmation:

- **Per-user data isolation is enforced in the service/repository layer, not just the UI.** Every query touching `user_library`, `user_watch_state`, `reviews`, `watch_events`, or `favorites` must be scoped to the authenticated user resolved from the JWT. Show catalog detail/list endpoints must verify `user_library` membership — never trust a user ID from the request body/path.
- **`watch_events` is append-only.** Never add an UPDATE or DELETE operation against this table, in the repository layer or anywhere else.
- **Cascade operations are one atomic transaction.** Marking or unmarking a season/show watched must update all descendant rows and write all corresponding `watch_events` rows inside a single transaction — no partial cascades.
- **Season/show-level unmark requires an explicit `confirm=true`.** Reject the request with `400` if it's missing — don't silently proceed or silently default it to true.
- **No rating roll-up.** Episode, season, and show ratings/reviews are fully independent. Never compute or cache a show's rating from its seasons' or episodes' ratings, or vice versa.
- **Rating precision is 0.5 increments between 0.5 and 5.0.** Validate this both client- and server-side.
- **No cross-user visibility, ever.** There are no social features. Don't add any endpoint, query, or UI element that exposes one user's data to another, even read-only, even for "leaderboard"-style features that might seem harmless.
- **TVmaze is called only from `show/tvmaze/TvmazeClient`.** No other class should call out to TVmaze directly. Search results are never persisted — only an explicit "add to library" creates global catalog rows (if missing) and a `user_library` link.
- **Frontend server state uses TanStack Query only.** All data from `api/*.ts` MUST be consumed via `useQuery`/`useMutation` hooks in `features/<feature>/hooks/`. Do not call `api/*` from pages or components directly, and do not store server responses in `useState`/`useEffect`. Form and UI-only state (inputs, modals) may use `useState`. JWT token persistence in `localStorage` is client session plumbing, not server state.

---

## 5. Task Tracking

Two levels:

- **Phase roadmap:** `openspec/TASKS.md` — ordered phases with acceptance criteria (e.g. task **3.2**).
- **Feature work:** `openspec/changes/<change-name>/tasks.md` — concrete steps for the current change.

When you complete a task in a change's `tasks.md`:
1. Verify it against its stated acceptance criteria (and the relevant scenarios in `openspec/specs/<domain>/spec.md` or the change's delta specs) before marking it done.
2. Check its box in that change's `tasks.md` (`- [ ]` → `- [x]`) in the same commit that completes it.
3. If a task turns out to need something not covered in the proposal/design/specs, resolve or flag that *before* checking the box — don't check it off with an undocumented gap.
4. When the change is complete, run `/opsx:archive` so its delta specs merge into `openspec/specs/` and the change folder moves to `openspec/changes/archive/`. Don't hand-edit `openspec/specs/` directly outside of this merge process.
5. If the change completes one or more phase tasks in `openspec/TASKS.md`, check those boxes in the same commit as the archive.

---

## 6. Commit Conventions

Use Conventional Commits, with the relevant `openspec/TASKS.md` phase task ID in brackets at the end of the subject line:

```
feat(watch): cascade mark-watched logic [3.2]
fix(auth): reject login for unverified users [1.2]
test(analytics): cover longest-to-watch edge cases [6.5]
chore(setup): add docker-compose for local postgres [0.2]
```

Use the standard types (`feat`, `fix`, `refactor`, `test`, `chore`, `docs`) and the feature name as scope (matching the package/folder name, e.g. `auth`, `show`, `watch`, `review`, `favorite`, `analytics`). If a change touches multiple tasks, list multiple IDs: `[3.2, 3.4]`.

---

## 7. When Something Is Ambiguous

Several assumptions from `docs/PRD.md` §9 are resolved — see **Resolved decisions** in `openspec/TASKS.md` (duplicate add → 409, orphan catalog delete, one-time TVmaze snapshot). One item remains open:

- Whether "Plan to Watch" applies only at the show level (currently implemented that way).

For anything else not fully specified in `openspec/specs/`, `docs/PRD.md`, `docs/ARCHITECTURE.md`, or `openspec/TASKS.md`: stop and ask, rather than picking a default and moving on.
