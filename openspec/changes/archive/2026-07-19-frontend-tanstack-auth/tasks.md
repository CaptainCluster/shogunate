## 1. Auth query infrastructure

- [x] 1.1 Create `frontend/src/features/auth/authKeys.ts`
- [x] 1.2 Create `frontend/src/features/auth/clearSession.ts` (cancelQueries → removeQueries → setAuthToken null)
- [x] 1.3 Create `frontend/src/lib/getErrorMessage.ts`; update imports across auth pages

## 2. TanStack hooks

- [x] 2.1 Add `useCurrentUser.ts` — `useQuery` for `/api/me` (enabled when token exists, refetchOnWindowFocus, staleTime 0, no 401 retry, clearSession on auth failure)
- [x] 2.2 Add `useAuthStorageSync.ts` — storage listener on `auth_token`; mount once in `App.tsx`
- [x] 2.3 Add mutation hooks: `useLogin`, `useRegister`, `useVerifyEmail`, `useResendVerification`, `useForgotPassword`, `useResetPassword`, `useLogout`

## 3. Refactor useAuth and App

- [x] 3.1 Rewrite `hooks/useAuth.tsx` as facade over TanStack hooks (same public API: user, isLoading, login, logout)
- [x] 3.2 Remove `AuthProvider` from `App.tsx`

## 4. Migrate auth pages and remove dead code

- [x] 4.1 Update LoginPage, RegisterPage, VerifyEmailPage, ForgotPasswordPage, ResetPasswordPage to use mutation hooks
- [x] 4.2 Remove dead code: AuthContext, AuthProvider, refreshUser, page-level authApi imports, getErrorMessage from useAuth
- [x] 4.3 Grep verify: no `AuthProvider`, `refreshUser`, or page-level `authApi` imports remain

## 5. Documentation guardrails

- [x] 5.1 Add TanStack-only bullet to `openspec/AGENTS.md` §4 Non-Negotiable Constraints
- [x] 5.2 Update `docs/ARCHITECTURE.md` §7.1 folder tree (features/*/hooks/) and add §7.2.1 with deprecated patterns table

## 6. Verification

- [x] 6.1 `pnpm build` passes
- [x] 6.2 Manual E2E: register → verify → login → protected route → logout
- [x] 6.3 Manual: invalid token in localStorage → reload → token cleared, redirect to login
- [x] 6.4 Manual: login user A → logout → login user B → email updates in Layout
- [x] 6.5 Manual: two tabs — logout in one → switch to other → session syncs via storage listener
