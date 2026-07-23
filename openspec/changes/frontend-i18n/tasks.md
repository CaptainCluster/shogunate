## 1. i18n infrastructure

- [x] 1.1 Add `i18next` and `react-i18next` to `frontend/package.json` and install
- [x] 1.2 Create `frontend/src/i18n/config.ts` — init i18next with `fallbackLng: 'en'`, read/write `localStorage` key `locale`, register EN/FI namespaces
- [x] 1.3 Create locale JSON scaffolding under `frontend/src/i18n/locales/{en,fi}/` with namespaces: `common`, `auth`, `library`, `watch`, `favorites`, `reviews`, `analytics`
- [x] 1.4 Import `./i18n/config` in `frontend/src/main.tsx` before app render

## 2. Language switch and layout

- [x] 2.1 Create `LanguageSwitch` component — EN/FI toggle, `i18n.changeLanguage`, persist to `localStorage`, update `document.documentElement.lang`
- [x] 2.2 Integrate `LanguageSwitch` into `Layout.tsx` header nav; migrate all Layout nav strings to `common` namespace
- [x] 2.3 Set `document.title` from translations on locale change; fix `index.html` placeholder title
- [x] 2.4 Migrate `HomePage.tsx` and `ProtectedRoute.tsx` strings to translations

## 3. Feature string migration

- [x] 3.1 Migrate auth pages (`LoginPage`, `RegisterPage`) to `auth` namespace
- [x] 3.2 Migrate library pages and components (`LibraryPage`, `SearchPage`, `ShowDetailPage`, `AboutPage`, `RemoveFromLibraryButton`) to `library` namespace
- [x] 3.3 Refactor `formatLibraryStatus.ts` to use translation keys
- [x] 3.4 Migrate watch components (`WatchButtonPair`, `SeasonProgress`) to `watch` namespace; use active locale for date formatting
- [x] 3.5 Migrate favorites components (`FavoriteToggle`, `FavoriteSuggestionsPanel`) to `favorites` namespace
- [x] 3.6 Migrate `ReviewEditor` to `reviews` namespace
- [x] 3.7 Migrate analytics page and all section components to `analytics` namespace; translate period enum labels (Month/Year/Custom)
- [x] 3.8 Refactor `formatDuration.ts` formatters to use translation keys for units and UTC labels
- [x] 3.9 Migrate shared components (`ConfirmDialog`, `StarRatingInput`, `StarRatingDisplay`) and all confirm-dialog call-site strings to translations

## 4. Finnish translations

- [x] 4.1 Provide complete Finnish translations in all `fi/*.json` namespace files (mirror every EN key)

## 5. Tests and verification

- [x] 5.1 Add `renderWithI18n` test helper wrapping i18next init with EN/FI resources
- [x] 5.2 Update existing tests that assert visible English strings to use `renderWithI18n` with `lng: 'en'`
- [x] 5.3 Add tests for `LanguageSwitch` (toggle, localStorage persistence) and one Finnish smoke test
- [x] 5.4 Run `pnpm lint`, `pnpm test`, and `pnpm build`; manually verify EN/FI switch across key pages

## 6. Archive

- [ ] 6.1 Archive change via `/opsx:archive`; add Phase **7.5 — Frontend i18n** to `openspec/TASKS.md` and check it off
