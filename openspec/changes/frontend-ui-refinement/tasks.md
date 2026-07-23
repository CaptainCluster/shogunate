## 1. Design tokens and global layout

- [x] 1.1 Add spacing scale (`--space-1` through `--space-12`), radius tokens (`--radius-sm`, `--radius-md`), breakpoint tokens (`--bp-tablet`, `--bp-desktop`), and semantic color tokens (`--color-error`) to `frontend/src/index.css`
- [x] 1.2 Remove Vite template constraints from `#root` (fixed 1126px width, side borders, global `text-align: center`); add `--content-max: 960px` token
- [x] 1.3 Create `frontend/src/styles/ui.css` with shared `.ui-button` (default, primary, ghost), `.ui-input`, `.ui-select`, and `.ui-card` classes using design tokens; import in `main.tsx`
- [x] 1.4 Update `Layout.css` to use tokens for header border and spacing; replace hardcoded `#333`

## 2. Responsive header and shell

- [x] 2.1 Add mobile nav toggle button and collapsible panel to `Layout.tsx` with `aria-expanded` and accessible label from i18n
- [x] 2.2 Style responsive header in `Layout.css`: hamburger below 768px, slide-down nav panel, 44px min tap targets for nav links
- [x] 2.3 Update `LanguageSwitch.css` to use design tokens instead of hardcoded `#444`/`#333`
- [x] 2.4 Create `HomePage.css` with left-aligned layout, vertical rhythm using spacing tokens; apply button-style links where appropriate

## 3. Shared UI adoption

- [x] 3.1 Apply `ui-input` and `ui-button` classes to auth forms in `LoginPage.tsx` and `RegisterPage.tsx`; update `auth.css` to use tokens
- [x] 3.2 Apply shared UI classes to library search input, status select, and action buttons in library pages
- [x] 3.3 Apply shared UI classes to analytics period toggles and date inputs in `analytics.css`
- [x] 3.4 Apply shared UI classes to watch buttons in `watch.css`; unify error colors to `--color-error` token

## 4. Feature page spacing and responsive layouts

- [x] 4.1 Update `LibraryPage.css`: consistent card padding/gaps from spacing tokens; responsive card layout below 768px
- [x] 4.2 Add responsive show detail header stacking (poster above metadata) in `LibraryPage.css` below 768px
- [x] 4.3 Update `watch.css`: stack episode rows on mobile (title/review above watch controls); increase vertical padding
- [x] 4.4 Update `analytics.css`: remove negative intro margin; stack bar chart rows on mobile; ensure stat grid adapts to 1–2 columns
- [x] 4.5 Update `reviews.css`, `favorites.css`, and `ConfirmDialog.css` for token-based spacing and dark-mode-safe borders
- [x] 4.6 Update `starRating.css` to use accent tokens for star colors

## 5. Polish and verification

- [x] 5.1 Add visible focus rings to all shared UI interactive elements; add subtle hover transitions (~150ms)
- [x] 5.2 Delete unused `frontend/src/App.css`
- [x] 5.3 Run `pnpm lint`, `pnpm test`, and `pnpm build`
- [x] 5.4 Manually verify layouts at 320px, 768px, and 1024px+ viewports in both EN and FI locales and light/dark mode

## 6. Archive

- [ ] 6.1 Archive change via `/opsx:archive`; add Phase **7.6 — Frontend UI refinement** to `openspec/TASKS.md` and check it off
