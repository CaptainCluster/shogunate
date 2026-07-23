# frontend-ui Specification

## Purpose
Cross-cutting frontend presentation: design tokens, shared UI primitives, spacing rhythm, responsive layouts, and dark-mode-safe styling.

## Requirements

### Requirement: Design token system
The frontend SHALL define CSS custom properties for colors, spacing, border radius, shadows, typography sizes, and responsive breakpoints in a global stylesheet. Feature and component styles MUST use these tokens instead of hardcoded values for colors and spacing where tokens exist.

#### Scenario: Spacing tokens are available globally
- **WHEN** any page or component stylesheet references spacing
- **THEN** it MAY use `--space-*` tokens defined in the global stylesheet
- **AND** ad-hoc pixel values for spacing are not required for new or updated rules

#### Scenario: Dark mode uses token overrides
- **WHEN** the user prefers dark color scheme via `prefers-color-scheme: dark`
- **THEN** color tokens (`--text`, `--bg`, `--border`, `--accent`, etc.) update consistently
- **AND** layout chrome and form controls do not rely on hardcoded light-only colors

### Requirement: Shared UI primitives
The frontend SHALL provide shared CSS classes for buttons, text inputs, selects, and cards that apply consistent padding, border radius, borders, focus rings, and accent styling across all pages.

#### Scenario: Form controls look consistent on auth pages
- **WHEN** a user views the login or register page
- **THEN** input fields and submit buttons use the shared UI styles
- **AND** their appearance matches search inputs and action buttons on library pages

#### Scenario: Focus state is visible
- **WHEN** a user focuses a shared UI button or input via keyboard
- **THEN** a visible focus indicator is displayed
- **AND** the indicator uses accent or border tokens

### Requirement: Full-width responsive layout shell
The application root layout MUST use the full viewport width up to a single content max-width token. The layout MUST NOT constrain content to a fixed pixel width with side borders inherited from the Vite starter template.

#### Scenario: Content uses available viewport width
- **WHEN** a user views any page on a viewport wider than the content max-width
- **THEN** page content is centered with horizontal padding
- **AND** no fixed-width side borders appear on the root element

#### Scenario: Text alignment is page-controlled
- **WHEN** a user views the home page or a feature page
- **THEN** text alignment is determined by the page or feature stylesheet
- **AND** the root element does not force center alignment on all content

### Requirement: Responsive header navigation
The application header navigation MUST adapt for narrow viewports. On viewports below the tablet breakpoint, navigation links and auth controls MUST be accessible via a collapsible menu without horizontal overflow.

#### Scenario: Mobile nav does not overflow
- **WHEN** a user views the application on a viewport narrower than 768px
- **THEN** the header does not produce horizontal scrolling
- **AND** all navigation links remain reachable

#### Scenario: Mobile nav toggle is accessible
- **WHEN** a user activates the mobile navigation toggle
- **THEN** the navigation panel opens or closes
- **AND** the toggle exposes an appropriate `aria-expanded` state

### Requirement: Responsive feature page layouts
Library, show detail, analytics, and auth pages MUST adapt their layouts for viewports below the tablet breakpoint. Multi-column layouts MUST stack into single-column layouts where necessary to prevent horizontal overflow and cramped content.

#### Scenario: Show detail header stacks on mobile
- **WHEN** a user views a show detail page on a viewport narrower than 768px
- **THEN** the poster and show metadata are displayed in a vertical stack
- **AND** no horizontal overflow occurs in the detail header

#### Scenario: Episode rows stack on mobile
- **WHEN** a user views episode rows on a show detail page on a viewport narrower than 768px
- **THEN** episode title and review content appear above watch controls
- **AND** watch controls are not constrained to a fixed narrow column

#### Scenario: Analytics bar chart stacks on mobile
- **WHEN** a user views an analytics bar chart on a viewport narrower than 768px
- **THEN** each bar row displays label, bar, and value in a vertical stack
- **AND** long translated labels wrap without truncation overflow

### Requirement: Touch-friendly interactive targets
On viewports below the tablet breakpoint, interactive controls in the header, forms, and primary action areas MUST provide a minimum tap target size of 44×44 CSS pixels.

#### Scenario: Mobile nav links are tappable
- **WHEN** a user interacts with navigation links in the mobile menu
- **THEN** each link has a tap target of at least 44×44 CSS pixels

#### Scenario: Primary buttons are tappable on mobile
- **WHEN** a user taps a primary action button (login, mark watched, period toggle) on a viewport narrower than 768px
- **THEN** the button's tap target is at least 44×44 CSS pixels

### Requirement: Consistent spacing rhythm
Pages MUST use the spacing token scale for section gaps, card padding, and list item separation. Negative margins used solely to tighten spacing against headings MUST NOT be used in updated page styles.

#### Scenario: Analytics page section spacing
- **WHEN** a user views the analytics page
- **THEN** consecutive sections are separated by consistent vertical spacing from the token scale
- **AND** the page intro text is not pulled upward against the page heading via negative margin

#### Scenario: Library list spacing
- **WHEN** a user views the library page
- **THEN** library cards in the list have consistent gap and internal padding from the token scale
