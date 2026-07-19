## Context

Foundation setup wired `QueryClientProvider` and `lib/queryClient.ts`, but no feature uses TanStack Query yet. The archived user-auth change implemented auth with `AuthProvider`, manual `authApi` calls, and `useState`/`useEffect` for `/api/me`. Architecture docs require all server state via `useQuery`/`useMutation` in feature hook folders.

## Goals / Non-Goals

**Goals:**
- Migrate auth server state to TanStack Query without changing user-visible auth behavior
- Establish reference pattern for show-library and future features
- Remove dead Context-based auth code
- Add session edge-case handling: multi-tab sync, logout cache clear, login+/me failure surfacing
- Document deprecated patterns so agents do not reintroduce Context/manual fetch

**Non-Goals:**
- Backend changes
- Auth error message or UX changes
- Vitest tests
- Global 401 interceptor (show-library may use `clearSession()` later)
- `persistQueryClient` for auth cache

## Decisions

### Hook layout: feature folder + facade

- Server hooks live in `frontend/src/features/auth/hooks/` (one `useQuery`/`useMutation` per `authApi` function)
- `hooks/useAuth.tsx` remains a thin facade for route guards and layout (`user`, `isLoading`, `login`, `logout`)
- Pages call mutation hooks directly; guards call `useAuth`

### Query keys

```ts
export const authKeys = {
  all: ['auth'] as const,
  me: () => [...authKeys.all, 'me'] as const,
}
```

No user email or ID in keys.

### Session teardown: `clearSession()`

Shared helper in `features/auth/clearSession.ts`:

1. `queryClient.cancelQueries({ queryKey: authKeys.all })`
2. `queryClient.removeQueries({ queryKey: authKeys.all })`
3. `setAuthToken(null)`

Used by `useLogout`, `/me` 401 handling, and future global 401 handler.

### `useCurrentUser`

- `useQuery` with key `authKeys.me()`
- `enabled` when JWT exists in `localStorage`
- `refetchOnWindowFocus: true`
- `staleTime: 0` (profile may change after verify)
- `retry`: no retry on 401; at most one retry otherwise
- On 401/error: call `clearSession()`
- `isLoading` for consumers: `isPending && isFetching` when query is enabled

### Multi-tab sync

`useAuthStorageSync` hook mounted once in `App.tsx` listens for `storage` events on `auth_token` and invalidates `authKeys.me()` when the token changes in another tab.

### Login flow

`useLogin` mutation:

1. Call `authApi.login`
2. `setAuthToken(response.token)`
3. Invalidate/refetch `authKeys.me()`
4. If refetch fails, reject mutation (fixes silent login failure)

### Logout flow

`useLogout` calls `clearSession()`.

### No AuthProvider

Remove React Context for user state. `QueryClientProvider` at app root is sufficient.

### `authApi.ts` unchanged

Fetch wrappers stay in `api/authApi.ts`; only hooks call them.

## Reference pattern (for future features)

```
api/<feature>Api.ts          → fetch wrappers only
features/<feature>/hooks/    → useQuery / useMutation per API function
features/<feature>/<keys>.ts → query key factory
pages                        → mutation hooks + useState for form UI
```

show-library and other features follow `features/auth/` as the template.

## Deprecated patterns (removed by this change)

| Dead pattern | Replacement |
|---|---|
| `AuthProvider` / React Context for user state | `QueryClientProvider` + `useCurrentUser()` |
| `refreshUser()` | `queryClient.invalidateQueries({ queryKey: authKeys.me() })` |
| `useState`/`useEffect` + `authApi.getCurrentUser()` | `useCurrentUser()` |
| Direct `authApi.*` in pages | `features/auth/hooks/use*.ts` |
| `getErrorMessage` in `useAuth.tsx` | `lib/getErrorMessage.ts` |

Archived user-auth Context approach is superseded; archive docs stay as historical record.

## Risks / Trade-offs

- **[Risk] Multi-tab stale cache** → Mitigated by `refetchOnWindowFocus` + `storage` listener
- **[Risk] Stale user after logout/login as different user** → Mitigated by `clearSession()` on logout
- **[Risk] Login/logout race** → Mitigated by `cancelQueries` in `clearSession()`
- **[Risk] ProtectedRoute flash** → Map `isLoading` to `isPending && isFetching` when enabled
- **[Risk] Agents reintroduce Context** → Mitigated by AGENTS.md §4 non-negotiable + ARCHITECTURE §7.2.1 deprecated table

## Migration Plan

1. Scaffold `authKeys`, `clearSession`, `getErrorMessage`
2. Add query + mutation hooks and `useAuthStorageSync`
3. Rewrite `useAuth` facade; remove `AuthProvider` from `App.tsx`
4. Migrate auth pages; delete dead code
5. Update documentation
6. Verify with `pnpm build` and manual E2E

## Open Questions

None.
