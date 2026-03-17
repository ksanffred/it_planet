# CLAUDE.md — Tramplin (IT Planet Career Platform)

## Project Overview

**Tramplin** is an interactive career platform that publicly displays vacancies, internships, mentoring programs, and career events (open days, hackathons, company lectures). Opportunities are grouped by categories, skills (tech stacks), and system tags, presented as cards. Offline opportunities (office work, in-person events) are shown on a map with exact addresses.

## Repository Structure

```
it_planet/
├── frontend/       # Nuxt 4 SPA/SSR
└── backend/
    └── tramplin/   # Spring Boot 4 REST API
```

## Tech Stack

### Frontend (`/frontend`)
- **Nuxt 4** + **Vue 3** + **TypeScript**
- **Vue Router 5** for routing
- **Yarn 4** as package manager
- **Oxlint** + **Oxfmt** (Rust-based linting and formatting)
- **Husky** for git hooks

### Backend (`/backend/tramplin`)
- **Spring Boot 4** + **Java 21**
- **Spring Web MVC** — REST API
- **Spring Data JPA** — ORM
- **Spring Validation** — input validation
- **Gradle 9** with Kotlin DSL
- Group: `ru.tramplin_itplanet`, artifact: `tramplin`

## Development Commands

### Frontend
```bash
cd frontend
yarn install       # install dependencies
yarn build         # production build
yarn lint          # lint with auto-fix
yarn format        # format with oxfmt
```
NEVER USE yarn dev only yarn build for testing if it compile or not

### Backend
```bash
cd backend/tramplin
./gradlew bootRun  # run dev server
./gradlew build    # build JAR
./gradlew test     # run tests
```

## Domain Concepts

- **Opportunity** — core entity: vacancy, internship, mentoring program, or career event
- **Card** — UI representation of an opportunity with full details
- **Category** — top-level grouping of opportunities (e.g., vacancies, events)
- **Tag** — system label attached to opportunities (skills, tech stacks, event types)
- **Offline opportunity** — requires physical presence; must include an address shown on a map

## Key Architectural Notes

- Frontend and backend are separate applications in a monorepo (no shared types/contracts yet)
- No database is configured yet — `application.properties` only sets `spring.application.name=tramplin`
- No Docker/CI/CD setup exists yet
- The frontend still has the default Nuxt welcome screen (`app/app.vue`)
- Map integration for offline opportunities is planned but not yet implemented
