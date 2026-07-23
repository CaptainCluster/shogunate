## Why

The frontend UI is English-only with hardcoded strings across every page and component. Finnish-speaking users need a localized experience, and a proper i18n foundation makes future languages tractable without scattering copy in source files.

## What Changes

- Add `i18next` + `react-i18next` with English (default) and Finnish translation files
- Move all frontend-owned UI text (~130 keys) into namespaced locale JSON files
- Add a language switch control in the header to toggle EN/FI
- Persist the chosen locale in `localStorage` and restore on load
- Make format helpers (library status, duration, dates) locale-aware via translation keys
- Update Vitest tests with an i18n test helper; add LanguageSwitch tests
- Set `document.documentElement.lang` and page title from the active locale

## Capabilities

### New Capabilities

- `i18n`: Cross-cutting frontend internationalization — supported locales, translation-backed UI copy, header language switch, persistence, and locale-aware formatting

### Modified Capabilities

_None — existing feature specs (auth, show, watch, etc.) remain unchanged; i18n is a new cross-cutting capability._

## Impact

- **Frontend:** new `src/i18n/` config and locale files; new `LanguageSwitch` component; string migration across all feature pages, shared components, and format helpers; test helper updates
- **Backend:** none
- **Dependencies:** `i18next`, `react-i18next` (new npm packages)
- **Docs:** add Phase 7.5 to `openspec/TASKS.md` on archive

## Non-goals

- Backend localization or API error-message translation
- Languages beyond English and Finnish
- Translating TVmaze/catalog content (show titles, overviews)
- RTL layout support
