## Why

The frontend UI works functionally but looks uneven: leftover Vite template constraints, ad-hoc spacing, browser-default form controls, and no responsive layouts make the app feel cramped on desktop and unusable on mobile/tablet. A CSS-only visual refresh will improve aesthetics and cross-device usability without changing existing UX flows.

## What Changes

- Expand design tokens in `index.css` (spacing scale, radius, breakpoints, semantic colors)
- Add shared UI styles (`ui.css`) for buttons, inputs, selects, and cards
- Remove Vite template layout constraints (`#root` fixed width, global center alignment)
- Normalize Layout header and navigation to tokens; add responsive hamburger nav for narrow viewports
- Apply consistent spacing rhythm across library, show detail, analytics, auth, and home pages
- Add mobile/tablet responsive rules for show detail headers, episode rows, analytics charts, and stat grids
- Unify hardcoded colors with design tokens for dark-mode consistency
- Add subtle focus/hover polish; remove unused `App.css`

## Capabilities

### New Capabilities

- `frontend-ui`: Cross-cutting frontend presentation — design tokens, shared UI primitives, spacing rhythm, responsive layouts, and dark-mode-safe styling

### Modified Capabilities

_None — feature behavior (auth, library, watch, analytics, etc.) is unchanged; this is presentation-only._

## Impact

- **Frontend:** `index.css`, new `styles/ui.css`, `Layout.css`/`Layout.tsx`, feature CSS files (`LibraryPage.css`, `analytics.css`, `watch.css`, `auth.css`, etc.), new `HomePage.css`
- **Backend:** none
- **Dependencies:** none (plain CSS only)
- **Tests:** visual/regression via existing Vitest component tests; no new test framework required

## Non-goals

- Changing UX flows, routes, or component behavior
- Introducing a CSS framework (Tailwind, MUI, etc.)
- New animations, illustrations, or branding beyond the existing purple accent
- Backend or API changes
- Translating or restyling TVmaze/catalog content (posters, show overviews)
