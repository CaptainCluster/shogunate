## Context

The frontend uses plain co-located CSS with partial design tokens in `frontend/src/index.css`. Vite starter leftovers (`#root` fixed width, side borders, global center alignment) conflict with the app's content-heavy pages. Spacing is ad-hoc across feature CSS files, form controls use browser defaults, and no feature CSS includes responsive breakpoints. The user chose a **refined minimal** direction: more whitespace, softer surfaces, existing purple accent, no UX flow changes.

Current styling files:
- Global: `index.css`
- Layout: `Layout.css`, `Layout.tsx`
- Features: `LibraryPage.css`, `analytics.css`, `watch.css`, `auth.css`, `reviews.css`, `favorites.css`
- Components: `ConfirmDialog.css`, `LanguageSwitch.css`, `starRating.css`
- Dead code: `App.css` (never imported)

## Goals / Non-Goals

**Goals:**
- Establish a consistent spacing scale and shared UI primitives (buttons, inputs, cards)
- Remove template layout constraints; let content use full viewport width with a single max-width token
- Make all primary pages usable on viewports ≥ 320px without horizontal overflow
- Unify hardcoded colors with CSS custom properties for dark-mode consistency
- Preserve all existing UX patterns, routes, and component behavior

**Non-Goals:**
- CSS framework migration (Tailwind, MUI, etc.)
- New animations, illustrations, or branding overhaul
- Backend changes
- Changing i18n keys or translation content

## Decisions

### 1. Hybrid CSS architecture (tokens + shared UI layer)

**Decision:** Extend `index.css` with spacing/radius/breakpoint tokens and add `frontend/src/styles/ui.css` for shared button, input, select, and card styles. Keep feature-specific CSS co-located.

**Alternatives considered:**
- *Tokens only:* Would require duplicating button/input rules across 8+ files.
- *Tailwind migration:* Large diff, new dependency, conflicts with existing co-located CSS convention.

**Rationale:** Minimal disruption, easy to expand, matches existing project patterns.

### 2. Spacing scale

**Decision:** Add `--space-1` through `--space-12` (0.25rem–3rem) and replace ad-hoc values in feature CSS. Remove negative margins (e.g. `.analytics-intro { margin-top: -1rem }`).

**Rationale:** Fixes the "cramped despite generous space" issue by making vertical rhythm explicit and consistent.

### 3. Responsive breakpoints

**Decision:** Mobile-first with two breakpoints as CSS custom properties:
- `--bp-tablet: 768px`
- `--bp-desktop: 1024px`

**Rationale:** Covers phone, tablet, and desktop without over-engineering breakpoint tiers.

### 4. Mobile navigation — hamburger with slide-down panel

**Decision:** Add a minimal JSX toggle in `Layout.tsx` (button + `aria-expanded`) with CSS-only panel animation. Nav links, language switch, and auth controls move into the collapsible panel below `--bp-tablet`.

**Alternatives considered:**
- *Flex-wrap nav:* Pure CSS but crowded with 8+ items and longer Finnish strings.
- *Bottom tab bar:* New UX pattern; out of scope.

**Rationale:** Cleanest refined-minimal fit; scales with i18n.

### 5. Button style — outlined primary with filled accent for key CTAs

**Decision:** Default buttons are outlined/ghost using `var(--border)`. Primary actions (Login, Submit, Mark watched) use filled accent background. Period toggles and secondary actions stay outlined.

**Rationale:** Refined minimal aesthetic; one clear CTA per screen.

### 6. Card elevation — subtle border + optional light shadow

**Decision:** Cards use `1px solid var(--border)`, `border-radius: var(--radius-md)`, and `box-shadow: var(--shadow)` at reduced opacity. No heavy drop shadows.

**Rationale:** Adds depth without box-heavy media-library feel.

### 7. Responsive layout stacking rules

| Area | Below 768px | At/above 768px |
|------|-------------|----------------|
| Show detail header | Poster stacked above metadata | Side-by-side flex |
| Episode rows | Title/review above watch controls (single column) | Two-column grid |
| Analytics bar chart | Label, bar, value stacked per row | Three-column grid |
| Analytics stat grid | 1–2 columns | `auto-fit minmax(160px, 1fr)` |
| Auth form | Full-width inputs with side padding | Centered, max 420px |

### 8. Touch targets

**Decision:** Interactive elements (nav links, buttons, period toggles) MUST have minimum 44×44px tap area on viewports below `--bp-tablet`, using padding rather than larger font sizes.

**Rationale:** WCAG 2.5.5 target size guidance for mobile usability.

## Risks / Trade-offs

- **[Risk] Hamburger nav requires small JSX change** → Keep toggle logic in `Layout.tsx` only; no new component library.
- **[Risk] Finnish strings overflow fixed grid columns** → Prefer `minmax()` and allow text wrap; stack layouts on mobile.
- **[Risk] Shared UI classes may conflict with existing class names** → Prefix with `ui-` (e.g. `ui-button`, `ui-input`).
- **[Risk] Visual-only change hard to test automatically** → Rely on existing Vitest tests passing; manual viewport checks during implementation.
- **[Trade-off] No visual regression test suite** → Accept manual verification for this change; consider Playwright visual tests in a future change.

## Migration Plan

1. Phase 1: Tokens + `ui.css` + `#root` fix (all pages benefit immediately)
2. Phase 2: Responsive header nav + auth/home polish
3. Phase 3: Feature page responsive CSS (library, show detail, analytics)
4. Phase 4: Dark mode pass, focus/hover polish, remove `App.css`

No deployment migration needed — CSS-only, no API or data changes. Rollback is a git revert.

## Open Questions

1. **Home page structure** — simple landing with improved spacing, or add explicit CTA buttons? (Leaning: improved spacing + button-style links using `ui-button`.)
2. **Library card density** — airier padding (fewer cards visible) vs current density with better internal spacing? (Leaning: same card count, more internal padding.)
3. **First PR scope** — foundation + nav only, or one complete vertical slice (library + show detail)? (Leaning: phased as above, all in one change/tasks list.)
