# ResOS — Multi-Tenant Restaurant Management SaaS

A production-grade, multi-tenant SaaS platform for restaurant operations management.

## Status

**Phase 9 — Analytics: Complete (Awaiting Review)**

## Getting Started

### Prerequisites

- Java 21+ (tested with OpenJDK 22)
- Node.js 20+
- Docker Desktop (for local PostgreSQL)

### 1. Start infrastructure

**Start Docker Desktop first** (must be running — wait until the whale icon in the system tray is steady).

```powershell
docker compose up -d
```

If you see `dockerDesktopLinuxEngine: The system cannot find the file specified`, Docker Desktop is not running. Open it from the Start menu, wait ~30 seconds, then retry.

### 2. Run backend

Use separate lines or `;` in PowerShell 5.x (`&&` only works in PowerShell 7+):

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

API available at `http://localhost:8080`

### 3. Run frontend

```powershell
cd frontend
npm start
```

App available at `http://localhost:4200`

### 4. Run tests

```powershell
cd backend; .\mvnw.cmd test
cd ..\frontend; npm run build; npx ng test --watch=false --browsers=ChromeHeadless
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Angular 20, TypeScript, Angular Material, NgRx, SCSS |
| Backend | Java 21, Spring Boot 3, Spring Security, JWT |
| Database | PostgreSQL |
| Cache | Redis |
| Testing | JUnit 5, Mockito, Jasmine, Karma, Cypress |
| DevOps | Docker, Docker Compose, Jenkins, GitHub Actions |
| Cloud | AWS-ready architecture |

## Documentation

| Document | Path |
|----------|------|
| System Architecture | [docs/architecture/01-system-overview.md](docs/architecture/01-system-overview.md) |
| Folder Structure | [docs/architecture/02-folder-structure.md](docs/architecture/02-folder-structure.md) |
| Database Schema | [docs/database/schema-design.md](docs/database/schema-design.md) |
| API Contracts | [docs/api/api-contracts.md](docs/api/api-contracts.md) |
| Development Roadmap | [docs/roadmap/development-roadmap.md](docs/roadmap/development-roadmap.md) |
| Phase 3 Test Report | [docs/testing/phase-3-test-report.md](docs/testing/phase-3-test-report.md) |
| Phase 4 Test Report | [docs/testing/phase-4-test-report.md](docs/testing/phase-4-test-report.md) |
| Phase 5 Test Report | [docs/testing/phase-5-test-report.md](docs/testing/phase-5-test-report.md) |
| Phase 6 Test Report | [docs/testing/phase-6-test-report.md](docs/testing/phase-6-test-report.md) |
| Phase 7 Test Report | [docs/testing/phase-7-test-report.md](docs/testing/phase-7-test-report.md) |
| Phase 8 Test Report | [docs/testing/phase-8-test-report.md](docs/testing/phase-8-test-report.md) |
| Phase 9 Test Report | [docs/testing/phase-9-test-report.md](docs/testing/phase-9-test-report.md) |

## Development Phases

1. **Project Architecture** — Complete
2. **Authentication & Authorization** — Complete
3. **Multi-Tenant Foundation** — Complete
4. **Restaurant Dashboard** — Complete
5. **Inventory Management** — Complete
6. **Employee Management** — Complete, awaiting review
7. **Reservation Management** — Complete, awaiting review
8. **Menu & Order Management** — Complete, awaiting review
9. **Analytics** — Complete, awaiting review
10. DevOps & Deployment

## Getting Started

> Project scaffolding (git init, Docker Compose, Spring Boot, Angular) begins in **Phase 2** after architecture approval.

## License

Proprietary — All rights reserved.
