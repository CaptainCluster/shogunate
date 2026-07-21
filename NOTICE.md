# Third-Party Notices

Shogunate incorporates or depends on the following third-party components.
See each project's repository for full license texts and copyright notices.

## Backend (direct runtime dependencies)

| Component | License | URL |
|-----------|---------|-----|
| Spring Boot / Spring Framework | Apache License 2.0 | https://spring.io/projects/spring-boot |
| Flyway | Apache License 2.0 | https://flywaydb.org/ |
| PostgreSQL JDBC Driver | BSD 2-Clause | https://jdbc.postgresql.org/ |
| springdoc-openapi | Apache License 2.0 | https://springdoc.org/ |
| JJWT (io.jsonwebtoken) | Apache License 2.0 | https://github.com/jwtk/jjwt |

Build-time only (not shipped in the runtime artifact): Lombok (MIT License).

## Frontend (direct production dependencies)

| Component | License | URL |
|-----------|---------|-----|
| React | MIT License | https://react.dev/ |
| TanStack Query | MIT License | https://tanstack.com/query |
| React Router | MIT License | https://reactrouter.com/ |

Development and build tooling (Vite, TypeScript, ESLint, Vitest, Testing Library,
etc.) is used to produce the frontend bundle and is not required to run the
application in production.

## External data

Show metadata and images are retrieved from the [TVmaze API](https://www.tvmaze.com/api)
and are licensed under [Creative Commons Attribution-ShareAlike 4.0 International
(CC BY-SA 4.0)](https://creativecommons.org/licenses/by-sa/4.0/). Attribution is
provided in the application **About** page and in project documentation.

## Apache License 2.0

Several dependencies are licensed under the Apache License, Version 2.0. A copy
of the Apache License 2.0 is available at http://www.apache.org/licenses/LICENSE-2.0
