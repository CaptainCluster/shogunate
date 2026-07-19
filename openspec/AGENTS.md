# AGENTS.md

Instructions for AI coding agents (e.g. Claude Code) working in this repository. Humans are welcome to read this too, but its primary audience is an agent about to make changes here.

This repo uses **OpenSpec** to drive spec-driven development. `openspec/specs/` is the live source of truth for system behavior — read it before starting any work. `docs/PRD.md`, `docs/ARCHITECTURE.md`, and `docs/TASKS.md` are the original human-authored planning documents that OpenSpec's specs were derived from; they remain useful as a readable overview and for implementation-level detail (package structure, DB schema, sequence flows) that intentionally does *not* live in `openspec/specs/` — but for current behavior, `openspec/specs/` wins if the two ever disagree.

**Workflow — use OpenSpec's commands, don't freehand it:**
- Start new work with `/opsx:propose` (or `/opsx:new`) to create a change under `openspec/changes/<change-name>/`.
- Implement via `/opsx:apply`, working through that change's own `tasks.md` — not `docs/TASKS.md`.
- Verify with `/opsx:verify` where applicable, then complete with `/opsx:archive`, which merges the change's delta specs into `openspec/specs/` and moves the change folder to `openspec/changes/archive/`.
- Delta specs (`openspec/changes/<name>/specs/<domain>/spec.md`) describe ADDED/MODIFIED/REMOVED requirements relative to the current spec in `openspec/specs/<domain>/spec.md` — write these, don't restate the whole spec.
- `docs/TASKS.md` is historical/reference only at this point (see its header note) — it maps out the original planned build order, but the actual unit of work an agent picks up and checks off is a change's own `tasks.md`, created via `/opsx:propose`.

Don't improvise scope or structure that contradicts `openspec/specs/` or the documents in `docs/` — if something seems missing or contradictory between them, flag it instead of guessing (see "When Something Is Ambiguous" below).

---

## 1. Repository Layout

```
/openspec       → OpenSpec workspace: specs/ (live source of truth), changes/ (work in progress + archive)
/docs           → PRD.md, ARCHITECTURE.md (implementation detail), TASKS.md (historical reference)
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

- Email: `firstmock.lastmock@local.test`
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

- **Per-user data isolation is enforced in the service/repository layer, not just the UI.** Every query touching `shows`, `seasons`, `episodes`, `reviews`, `watch_events`, or `favorites` must be scoped to the authenticated user resolved from the JWT — never trust a user ID from the request body/path.
- **`watch_events` is append-only.** Never add an UPDATE or DELETE operation against this table, in the repository layer or anywhere else.
- **Cascade operations are one atomic transaction.** Marking or unmarking a season/show watched must update all descendant rows and write all corresponding `watch_events` rows inside a single transaction — no partial cascades.
- **Season/show-level unmark requires an explicit `confirm=true`.** Reject the request with `400` if it's missing — don't silently proceed or silently default it to true.
- **No rating roll-up.** Episode, season, and show ratings/reviews are fully independent. Never compute or cache a show's rating from its seasons' or episodes' ratings, or vice versa.
- **Rating precision is 0.5 increments between 0.5 and 5.0.** Validate this both client- and server-side.
- **No cross-user visibility, ever.** There are no social features. Don't add any endpoint, query, or UI element that exposes one user's data to another, even read-only, even for "leaderboard"-style features that might seem harmless.
- **TMDb is called only from `show/tmdb/TmdbClient`.** No other class should call out to TMDb directly. Search results are never persisted — only an explicit "add to library" action writes a snapshot.

---

## 5. Task Tracking

Work is tracked in the active change's own `openspec/changes/<change-name>/tasks.md`, not `docs/TASKS.md`. When you complete a task:
1. Verify it against its stated acceptance criteria (and the relevant scenarios in `openspec/specs/<domain>/spec.md` or the change's delta specs) before marking it done.
2. Check its box in that change's `tasks.md` (`- [ ]` → `- [x]`) in the same commit that completes it.
3. If a task turns out to need something not covered in the proposal/design/specs, resolve or flag that *before* checking the box — don't check it off with an undocumented gap.
4. When the change is complete, run `/opsx:archive` so its delta specs merge into `openspec/specs/` and the change folder moves to `openspec/changes/archive/`. Don't hand-edit `openspec/specs/` directly outside of this merge process.

`docs/TASKS.md` reflects the original planned build order and is kept for historical reference — it is not updated as work proceeds.

---

## 6. Commit Conventions

Use Conventional Commits, with the relevant `TASKS.md` task ID in brackets at the end of the subject line:

```
feat(watch): cascade mark-watched logic [3.2]
fix(auth): reject login for unverified users [1.2]
test(analytics): cover longest-to-watch edge cases [6.5]
chore(setup): add docker-compose for local postgres [0.2]
```

Use the standard types (`feat`, `fix`, `refactor`, `test`, `chore`, `docs`) and the feature name as scope (matching the package/folder name, e.g. `auth`, `show`, `watch`, `review`, `favorite`, `analytics`). If a change touches multiple tasks, list multiple IDs: `[3.2, 3.4]`.

---

## 7. When Something Is Ambiguous

Three assumptions are already flagged as open in `docs/PRD.md` §9 and carried into `docs/ARCHITECTURE.md` §9 — don't silently resolve these yourself if you hit them (e.g. while implementing Tasks 2.4/2.5):
1. Whether "Plan to Watch" applies only at the show level.
2. Whether show metadata is a one-time snapshot vs. kept live-synced with TMDb.
3. Exact cascade-delete behavior when a show is removed from a library.

For these, and for anything else not fully specified in PRD/ARCHITECTURE/TASKS: stop and ask, rather than picking a default and moving on. A wrong guess here tends to compound across several tasks before anyone notices.
