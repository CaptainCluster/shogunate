## ADDED Requirements

### Requirement: Supported locales
The frontend SHALL support English (`en`) as the default locale and Finnish (`fi`) as an additional locale. When no saved language preference exists, the active locale MUST be `en`.

#### Scenario: First visit defaults to English
- **WHEN** a user loads the application with no saved locale preference
- **THEN** the active locale is English
- **AND** all frontend-owned UI text is displayed in English

#### Scenario: Saved Finnish preference is restored
- **WHEN** a user loads the application with a saved locale preference of `fi`
- **THEN** the active locale is Finnish
- **AND** all frontend-owned UI text is displayed in Finnish

### Requirement: Translation-backed UI copy
All frontend-owned visible UI text MUST be sourced from the i18n translation files. This includes navigation labels, page headings, button and link text, form labels, placeholders, loading and empty states, confirmation dialog titles and messages, aria-labels, and formatted display strings produced by frontend format helpers.

Content originating from external or backend sources (show titles, overviews, TVmaze metadata, and backend API error messages) MUST NOT be translated by the frontend.

#### Scenario: Navigation labels come from translations
- **WHEN** the user views the application header in English
- **THEN** navigation link labels are rendered from the English translation files
- **WHEN** the user switches to Finnish
- **THEN** the same navigation link labels are rendered from the Finnish translation files

#### Scenario: Backend error messages pass through unchanged
- **WHEN** an API request fails and the backend returns an error message
- **THEN** the frontend displays the backend message as returned
- **AND** does not replace it with a translated equivalent

### Requirement: Language switch in header
The application header SHALL provide a control that allows the user to switch between English and Finnish. Switching MUST update all visible frontend-owned UI text immediately without a full page reload.

#### Scenario: Switch to Finnish updates visible UI
- **GIVEN** the active locale is English
- **WHEN** the user activates the language switch to select Finnish
- **THEN** the active locale becomes Finnish
- **AND** visible frontend-owned UI text updates to Finnish without reloading the page

#### Scenario: Switch to English updates visible UI
- **GIVEN** the active locale is Finnish
- **WHEN** the user activates the language switch to select English
- **THEN** the active locale becomes English
- **AND** visible frontend-owned UI text updates to English without reloading the page

### Requirement: Locale persistence
The user's chosen locale MUST be persisted in browser storage and restored on subsequent visits.

#### Scenario: Locale persists across sessions
- **GIVEN** the user selects Finnish via the language switch
- **WHEN** the user closes and reopens the application in the same browser
- **THEN** the active locale is Finnish

### Requirement: Document language attribute
The frontend MUST set `document.documentElement.lang` to match the active locale whenever the locale changes or on initial load.

#### Scenario: Lang attribute reflects active locale
- **WHEN** the active locale is Finnish
- **THEN** `document.documentElement.lang` is `fi`
- **WHEN** the active locale is English
- **THEN** `document.documentElement.lang` is `en`

### Requirement: Locale-aware formatting
Frontend format helpers for library status labels, duration, percentages, and displayed dates MUST produce output appropriate to the active locale using translation keys or locale-aware formatting APIs bound to the active language.

#### Scenario: Library status label follows locale
- **WHEN** a show's library status is `PLAN_TO_WATCH` and the active locale is Finnish
- **THEN** the displayed status label is the Finnish translation for plan-to-watch

#### Scenario: Duration formatting follows locale
- **WHEN** a duration is formatted for display and the active locale is Finnish
- **THEN** the formatted output uses Finnish unit labels from the translation files
