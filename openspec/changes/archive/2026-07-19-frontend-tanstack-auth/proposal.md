## Why

TanStack Query is installed and wired at the app root, but auth server state still uses manual `fetch` + React Context (`AuthProvider`, `useState`, `useEffect`). This violates `docs/ARCHITECTURE.md` §7.2 and `openspec/AGENTS.md`. Aligning auth now establishes the data-fetching pattern before show-library and avoids two coexisting frontend approaches.

## What Changes

- Add TanStack Query hooks under `frontend/src/features/auth/hooks/` (one hook per `authApi` function)
- Add `authKeys.ts` query key factory and `clearSession.ts` session teardown helper
- Refactor `useAuth` to a thin facade over TanStack hooks; remove `AuthProvider`
- Update all auth pages to use mutation hooks instead of direct `authApi` calls
- Move `getErrorMessage` to `lib/getErrorMessage.ts`
- Remove dead code: Context, `refreshUser`, page-level `authApi` imports
- Strengthen documentation: TanStack-only rule in `AGENTS.md` §4; deprecated patterns in `ARCHITECTURE.md` §7.2.1

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — no backend or user-auth behavioral changes. Implements existing architecture constraints; `openspec/specs/user-auth/spec.md` unchanged.

## Non-goals

- Backend or API changes
- Auth UX or error message changes
- Vitest test suite (verify via `pnpm build` + manual E2E)
- show-library hooks (future changes follow the auth reference pattern)
- `persistQueryClient` for auth data
- Global 401 handler for non-auth API calls (deferred to show-library; `clearSession()` helper prepares for it)

## Impact

- **Frontend**: New `features/auth/hooks/`, `authKeys.ts`, `clearSession.ts`; rewrite `hooks/useAuth.tsx`; update `App.tsx` and five auth pages; extract `lib/getErrorMessage.ts`
- **Documentation**: `openspec/AGENTS.md`, `docs/ARCHITECTURE.md` §7.1 and §7.2.1
- **Unchanged**: `api/authApi.ts`, `api/client.ts`, backend
