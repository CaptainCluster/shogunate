# Notices

## Shogunate

Copyright (C) 2026 Ville Saloranta.

Shogunate is free software: you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see <https://www.gnu.org/licenses/>.

The full license text is in [LICENSE.md](LICENSE.md).

## Third-party software

Shogunate incorporates or depends on the following third-party components.
See each project's repository for full license texts and copyright notices.

### Backend (direct runtime dependencies)

| Component | License | URL |
|-----------|---------|-----|
| Spring Boot / Spring Framework | Apache License 2.0 | https://spring.io/projects/spring-boot |
| Flyway | Apache License 2.0 | https://flywaydb.org/ |
| PostgreSQL JDBC Driver | BSD 2-Clause | https://jdbc.postgresql.org/ |
| springdoc-openapi | Apache License 2.0 | https://springdoc.org/ |
| JJWT (io.jsonwebtoken) | Apache License 2.0 | https://github.com/jwtk/jjwt |

Build-time only (not shipped in the runtime artifact): Lombok (MIT License).

### Frontend (direct production dependencies)

| Component | License | URL |
|-----------|---------|-----|
| React | MIT License | https://react.dev/ |
| TanStack Query | MIT License | https://tanstack.com/query |
| React Router | MIT License | https://reactrouter.com/ |
| i18next | MIT License | https://www.i18next.com/ |
| react-i18next | MIT License | https://react.i18next.com/ |

Development and build tooling (Vite, TypeScript, ESLint, Vitest, Testing Library,
etc.) is used to produce the frontend bundle and is not required to run the
application in production.

### Development infrastructure

Local development may use container images that are not bundled with the
application, including PostgreSQL (PostgreSQL License).

## External data

Show metadata and images are retrieved from the [TVmaze API](https://www.tvmaze.com/api)
and are licensed under [Creative Commons Attribution-ShareAlike 4.0 International
(CC BY-SA 4.0)](https://creativecommons.org/licenses/by-sa/4.0/). Attribution is
provided in the application **About** page and in project documentation.

## Credits

The Cursor agent rule files `backend-architect.mdc` and `frontend-developer.mdc`
are adapted from [agency-agents](https://github.com/msitarzewski/agency-agents)
by msitarzewski (MIT License).

## Reference license texts

### GNU General Public License v3.0

Full text: [LICENSE.md](LICENSE.md) or <https://www.gnu.org/licenses/gpl-3.0.html>

### Apache License 2.0

Several dependencies are licensed under the Apache License, Version 2.0. A copy
of the Apache License 2.0 is available at
<http://www.apache.org/licenses/LICENSE-2.0>

### MIT License

Several dependencies are licensed under the MIT License. A summary is available
at <https://opensource.org/license/MIT>
