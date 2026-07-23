## Context

The React frontend has no i18n infrastructure today. All UI copy (~130 unique strings) is hardcoded English across layout, auth, library, watch, favorites, reviews, analytics, and shared components. Format helpers (`formatLibraryStatus`, `formatDuration`) and inline `Intl.DateTimeFormat` calls also embed English-specific output.

Phase 7 cross-cutting polish is the right home for this work. Backend API error messages remain English and pass through unchanged.

## Goals / Non-Goals

**Goals:**

- Support `en` (default) and `fi` locales via `i18next` + `react-i18next`
- All frontend-owned UI text sourced from translation files (labels, buttons, nav, empty/loading states, aria-labels, placeholders, confirm-dialog copy, format helpers)
- Header language switch toggling EN/FI with immediate UI update (no reload)
- Persist locale in `localStorage`; restore on next visit
- Update `document.documentElement.lang` and page title from active locale
- Vitest coverage with `renderWithI18n` helper and LanguageSwitch tests

**Non-Goals:**

- Backend localization or API error-code mapping
- Languages beyond EN/FI
- Translating TVmaze/catalog content (show titles, overviews)
- RTL layout support

## Decisions

### Library: `i18next` + `react-i18next`

**Decision:** Use the standard React i18n stack.

**Alternatives considered:** Lingui (compile-time extraction — heavier setup), react-intl (FormatJS — viable but less common in this codebase's style). `react-i18next` matches the project's straightforward component patterns and needs no build-time extraction step.

### Translation file layout: namespaced JSON by feature

**Decision:**

```
frontend/src/i18n/
  config.ts
  locales/
    en/  fi/
      common.json      # nav, layout, confirm defaults, loading
      auth.json
      library.json
      watch.json
      favorites.json
      reviews.json
      analytics.json
```

**Key convention:** dot-separated paths within namespaces, e.g. `library.status.planToWatch`, `analytics.period.month`.

**Rationale:** ~130 keys stay manageable; feature namespaces mirror `frontend/src/features/` and limit merge conflicts.

### Persistence: `localStorage` key `locale`

**Decision:** On init, read `localStorage.getItem('locale')`; if `en` or `fi`, use it; otherwise default to `en`. On switch, call `i18n.changeLanguage()` and write back.

**Rationale:** User confirmed persistence across sessions.

### Language switch: two-state toggle in header

**Decision:** New `LanguageSwitch` component in `Layout.tsx` nav, before auth links. Shows EN/FI with active-state styling. Uses `aria-label` from translations and `aria-pressed` on the active option.

**Alternatives considered:** Dropdown — rejected; only two locales and user requested a switch button.

### Format helpers: accept translation context

**Decision:**

- `formatLibraryStatus(status, t)` — map enum to translation key
- `formatDuration(seconds, t)` / `formatPercent` / date formatters — unit suffixes and labels via i18n keys
- `WatchButtonPair` — use `i18n.language` for `Intl.DateTimeFormat` instead of hardcoded `'en-US'`

**Rationale:** Keeps formatting logic centralized while output follows active locale.

### Error messages: frontend fallbacks only

**Decision:** `getErrorMessage()` unchanged; translate caller-supplied fallback strings via `t(...)` at each call site. Backend `ApiError.message` values display as returned.

### Testing: `renderWithI18n` wrapper

**Decision:** Test helper initializes a minimal i18n instance with EN (default) and FI resources. Existing tests render with `lng: 'en'` to keep assertions stable. Add LanguageSwitch tests and one Finnish smoke assertion.

**Alternatives considered:** Switch all tests to `data-testid` — rejected as unnecessary churn; English default in tests is sufficient.

### Bootstrap order

**Decision:** Import `./i18n/config` in `main.tsx` before `App` render. No extra provider needed beyond i18next init (react-i18next hooks work after init).

## Risks / Trade-offs

- **[Large diff across many files]** → Migrate feature-by-feature in tasks; run `pnpm test` after each batch
- **[Missing strings at runtime]** → Use i18n `saveMissing: false` in prod; complete FI files before merge; smoke-test both locales manually
- **[Finnish translation quality]** → Provide complete FI JSON during implementation; brand name "Shogunate" and "TVmaze" stay untranslated
- **[Test brittleness on visible text]** → Default test locale to `en`; use `data-testid` only where text coupling is problematic

## Migration Plan

1. Install deps and scaffold i18n config + empty locale files
2. Add LanguageSwitch; translate Layout/nav first (visible immediately)
3. Migrate remaining features in dependency order: auth → library → watch → favorites → reviews → analytics → shared
4. Fill Finnish translations
5. Update tests; verify `pnpm build`, `pnpm lint`, `pnpm test`
6. No backend deploy or DB migration required; frontend-only rollout

## Open Questions

_None — locale persistence and backend-error scope confirmed with stakeholder._
