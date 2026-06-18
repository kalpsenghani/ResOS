# ResOS — Folder Structure

> **Phase 1 Deliverable** | Project Organization  
> **Status:** Awaiting Review

---

## 1. Monorepo Root Structure

```
ResOS/
├── .github/
│   └── workflows/              # GitHub Actions CI pipelines
├── docs/                       # Architecture, API, database docs
│   ├── architecture/
│   ├── api/
│   ├── database/
│   └── roadmap/
├── backend/                    # Spring Boot 3 API
├── frontend/                   # Angular 20 SPA
├── docker/                     # Dockerfiles, compose, nginx config
├── jenkins/                    # Jenkins pipeline definitions
├── scripts/                    # Dev utilities, seed data, migrations helper
├── .gitignore
├── docker-compose.yml          # Local dev environment
├── docker-compose.prod.yml     # Production-like stack
└── README.md
```

---

## 2. Backend Structure (`backend/`)

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/resos/
│   │   │   ├── ResOsApplication.java
│   │   │   │
│   │   │   ├── config/                    # Spring configuration
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── TenantFilterConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── AuditConfig.java
│   │   │   │
│   │   │   ├── shared/                    # Cross-cutting concerns
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── BusinessException.java
│   │   │   │   │   ├── TenantAccessDeniedException.java
│   │   │   │   │   └── ErrorResponse.java
│   │   │   │   ├── tenant/
│   │   │   │   │   ├── TenantContext.java
│   │   │   │   │   ├── TenantContextHolder.java
│   │   │   │   │   ├── TenantFilter.java
│   │   │   │   │   └── TenantAware.java
│   │   │   │   ├── security/
│   │   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   └── UserPrincipal.java
│   │   │   │   ├── audit/
│   │   │   │   │   ├── Auditable.java
│   │   │   │   │   ├── AuditListener.java
│   │   │   │   │   └── AuditLogService.java
│   │   │   │   └── pagination/
│   │   │   │       ├── PageRequest.java
│   │   │   │       └── PageResponse.java
│   │   │   │
│   │   │   └── modules/                   # Feature modules (DDD bounded contexts)
│   │   │       │
│   │   │       ├── auth/
│   │   │       │   ├── controller/
│   │   │       │   │   └── AuthController.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── LoginRequest.java
│   │   │       │   │   ├── LoginResponse.java
│   │   │       │   │   └── RefreshTokenRequest.java
│   │   │       │   ├── service/
│   │   │       │   │   └── AuthService.java
│   │   │       │   └── repository/
│   │   │       │       └── RefreshTokenRepository.java
│   │   │       │
│   │   │       ├── tenant/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── domain/
│   │   │       │   │   ├── Tenant.java
│   │   │       │   │   └── TenantStatus.java
│   │   │       │   ├── service/
│   │   │       │   └── repository/
│   │   │       │
│   │   │       ├── user/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── domain/
│   │   │       │   │   ├── User.java
│   │   │       │   │   ├── Role.java
│   │   │       │   │   └── Permission.java
│   │   │       │   ├── service/
│   │   │       │   └── repository/
│   │   │       │
│   │   │       ├── restaurant/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── domain/
│   │   │       │   ├── service/
│   │   │       │   └── repository/
│   │   │       │
│   │   │       ├── inventory/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── domain/
│   │   │       │   │   ├── InventoryItem.java
│   │   │       │   │   ├── InventoryTransaction.java
│   │   │       │   │   └── StockAlert.java
│   │   │       │   ├── service/
│   │   │       │   └── repository/
│   │   │       │
│   │   │       ├── employee/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── domain/
│   │   │       │   │   ├── Employee.java
│   │   │       │   │   └── EmployeeSchedule.java
│   │   │       │   ├── service/
│   │   │       │   └── repository/
│   │   │       │
│   │   │       ├── reservation/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── domain/
│   │   │       │   │   ├── Reservation.java
│   │   │       │   │   └── RestaurantTable.java
│   │   │       │   ├── service/
│   │   │       │   └── repository/
│   │   │       │
│   │   │       ├── menu/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── domain/
│   │   │       │   │   ├── MenuCategory.java
│   │   │       │   │   ├── MenuItem.java
│   │   │       │   │   └── MenuItemModifier.java
│   │   │       │   ├── service/
│   │   │       │   └── repository/
│   │   │       │
│   │   │       ├── order/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── domain/
│   │   │       │   │   ├── Order.java
│   │   │       │   │   ├── OrderItem.java
│   │   │       │   │   └── OrderStatusHistory.java
│   │   │       │   ├── service/
│   │   │       │   └── repository/
│   │   │       │
│   │   │       ├── analytics/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   └── service/
│   │   │       │
│   │   │       ├── notification/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── domain/
│   │   │       │   ├── service/
│   │   │       │   └── repository/
│   │   │       │
│   │   │       ├── subscription/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── domain/
│   │   │       │   ├── service/
│   │   │       │   └── repository/
│   │   │       │
│   │   │       └── audit/
│   │   │           ├── controller/
│   │   │           ├── dto/
│   │   │           ├── domain/
│   │   │           │   └── AuditLog.java
│   │   │           ├── service/
│   │   │           └── repository/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/
│   │           └── migration/               # Flyway migrations
│   │               ├── V1__create_tenants.sql
│   │               ├── V2__create_users_roles.sql
│   │               └── ...
│   │
│   └── test/
│       ├── java/com/resos/
│       │   ├── integration/                 # Testcontainers integration tests
│       │   │   ├── TenantIsolationIT.java
│       │   │   ├── AuthControllerIT.java
│   │   │   │   └── ...
│       │   └── unit/                        # Unit tests mirror main structure
│       │       ├── auth/
│       │       ├── tenant/
│       │       └── ...
│       └── resources/
│           └── application-test.yml
│
├── pom.xml
└── Dockerfile
```

### Backend Module Convention

Each feature module follows the same internal structure:

```
module/
├── controller/     # REST endpoints, @PreAuthorize
├── dto/            # Request/Response records
├── domain/         # JPA entities, enums, value objects
├── service/        # Business logic, @Transactional
└── repository/     # Spring Data JPA interfaces
```

---

## 3. Frontend Structure (`frontend/`)

```
frontend/
├── src/
│   ├── app/
│   │   ├── app.config.ts                   # Application providers
│   │   ├── app.routes.ts                   # Root routes
│   │   ├── app.component.ts
│   │   │
│   │   ├── core/                           # Singleton services, guards, interceptors
│   │   │   ├── auth/
│   │   │   │   ├── guards/
│   │   │   │   │   ├── auth.guard.ts
│   │   │   │   │   └── role.guard.ts
│   │   │   │   ├── interceptors/
│   │   │   │   │   ├── auth.interceptor.ts
│   │   │   │   │   ├── tenant.interceptor.ts
│   │   │   │   │   └── error.interceptor.ts
│   │   │   │   └── services/
│   │   │   │       └── auth.service.ts
│   │   │   ├── tenant/
│   │   │   │   └── tenant.service.ts
│   │   │   ├── theme/
│   │   │   │   └── theme.service.ts
│   │   │   └── api/
│   │   │       └── api.service.ts
│   │   │
│   │   ├── shared/                         # Reusable UI components & utilities
│   │   │   ├── components/
│   │   │   │   ├── ui/                     # Design system primitives
│   │   │   │   │   ├── button/
│   │   │   │   │   ├── card/
│   │   │   │   │   ├── data-table/
│   │   │   │   │   ├── dialog/
│   │   │   │   │   ├── form-field/
│   │   │   │   │   ├── kpi-widget/
│   │   │   │   │   ├── loading-spinner/
│   │   │   │   │   ├── page-header/
│   │   │   │   │   ├── sidebar/
│   │   │   │   │   ├── status-badge/
│   │   │   │   │   └── toast/
│   │   │   │   └── layout/
│   │   │   │       ├── dashboard-layout/
│   │   │   │       ├── auth-layout/
│   │   │   │       └── empty-state/
│   │   │   ├── pipes/
│   │   │   ├── directives/
│   │   │   └── models/                     # Shared TypeScript interfaces
│   │   │       ├── api-response.model.ts
│   │   │       ├── pagination.model.ts
│   │   │       └── user.model.ts
│   │   │
│   │   ├── store/                          # NgRx global state
│   │   │   ├── auth/
│   │   │   │   ├── auth.actions.ts
│   │   │   │   ├── auth.reducer.ts
│   │   │   │   ├── auth.effects.ts
│   │   │   │   ├── auth.selectors.ts
│   │   │   │   └── auth.state.ts
│   │   │   ├── tenant/
│   │   │   │   ├── tenant.actions.ts
│   │   │   │   ├── tenant.reducer.ts
│   │   │   │   ├── tenant.effects.ts
│   │   │   │   └── tenant.selectors.ts
│   │   │   ├── ui/
│   │   │   │   ├── ui.actions.ts
│   │   │   │   ├── ui.reducer.ts
│   │   │   │   └── ui.selectors.ts
│   │   │   └── index.ts                    # provideStore, provideEffects
│   │   │
│   │   └── features/                       # Lazy-loaded feature modules
│   │       ├── auth/
│   │       │   ├── login/
│   │       │   │   └── login.component.ts
│   │       │   ├── register/
│   │       │   │   └── register.component.ts
│   │       │   └── auth.routes.ts
│   │       │
│   │       ├── dashboard/
│   │       │   ├── dashboard.component.ts
│   │       │   ├── components/
│   │       │   │   ├── kpi-cards/
│   │       │   │   ├── revenue-chart/
│   │       │   │   └── recent-orders/
│   │       │   └── dashboard.routes.ts
│   │       │
│   │       ├── inventory/
│   │       │   ├── inventory-list/
│   │       │   ├── inventory-form/
│   │       │   ├── stock-alerts/
│   │       │   ├── services/
│   │       │   │   └── inventory.service.ts
│   │       │   └── inventory.routes.ts
│   │       │
│   │       ├── employees/
│   │       │   ├── employee-list/
│   │       │   ├── employee-form/
│   │       │   ├── schedule/
│   │       │   ├── services/
│   │       │   └── employees.routes.ts
│   │       │
│   │       ├── reservations/
│   │       │   ├── reservation-list/
│   │       │   ├── reservation-calendar/
│   │       │   ├── table-management/
│   │       │   ├── services/
│   │       │   └── reservations.routes.ts
│   │       │
│   │       ├── menu/
│   │       │   ├── menu-builder/
│   │       │   ├── category-list/
│   │       │   ├── menu-item-form/
│   │       │   ├── services/
│   │       │   └── menu.routes.ts
│   │       │
│   │       ├── orders/
│   │       │   ├── order-list/
│   │       │   ├── order-detail/
│   │       │   ├── order-create/
│   │       │   ├── services/
│   │       │   └── orders.routes.ts
│   │       │
│   │       ├── analytics/
│   │       │   ├── revenue/
│   │       │   ├── inventory-analytics/
│   │       │   ├── employee-analytics/
│   │       │   └── analytics.routes.ts
│   │       │
│   │       └── settings/
│   │           ├── tenant-settings/
│   │           ├── profile/
│   │           └── settings.routes.ts
│   │
│   ├── assets/
│   │   ├── icons/
│   │   └── images/
│   │
│   ├── styles/
│   │   ├── _variables.scss                 # Design tokens
│   │   ├── _mixins.scss
│   │   ├── _typography.scss
│   │   ├── _themes.scss                    # Light + Dark theme maps
│   │   ├── _utilities.scss
│   │   └── styles.scss                     # Global entry
│   │
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   │
│   ├── index.html
│   └── main.ts
│
├── cypress/
│   ├── e2e/
│   │   ├── auth/
│   │   ├── dashboard/
│   │   └── ...
│   ├── fixtures/
│   └── support/
│
├── angular.json
├── package.json
├── tsconfig.json
├── karma.conf.js
└── Dockerfile
```

---

## 4. Docker Structure (`docker/`)

```
docker/
├── nginx/
│   ├── nginx.conf                          # Reverse proxy config
│   └── ssl/
├── postgres/
│   └── init.sql                            # Dev seed data
└── redis/
    └── redis.conf
```

---

## 5. Documentation Structure (`docs/`)

```
docs/
├── architecture/
│   ├── 01-system-overview.md
│   ├── 02-folder-structure.md
│   └── 03-tenant-isolation.md              # Deep dive (Phase 3)
├── database/
│   ├── schema-design.md
│   └── erd.md
├── api/
│   ├── api-contracts.md
│   └── openapi/                            # Generated OpenAPI specs
├── roadmap/
│   └── development-roadmap.md
└── testing/
    └── test-strategy.md
```

---

## 6. Git Flow Branch Strategy

```
main                    ← Production releases
  └── develop           ← Integration branch
        ├── feature/auth-jwt
        ├── feature/multi-tenant-foundation
        ├── feature/dashboard-ui
        ├── feature/inventory-management
        └── ...
```

### Branch Naming Convention

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feature/{module}-{description}` | `feature/auth-jwt` |
| Bugfix | `bugfix/{issue-id}-{description}` | `bugfix/42-tenant-leak` |
| Hotfix | `hotfix/{description}` | `hotfix/token-expiry` |
| Release | `release/v{major}.{minor}.{patch}` | `release/v1.0.0` |

### Commit Message Convention (Conventional Commits)

```
feat(auth): implement JWT authentication and refresh token workflow
fix(tenant): prevent cross-tenant data access in inventory queries
test(inventory): add tenant isolation integration tests
docs(api): define reservation management endpoints
chore(docker): add PostgreSQL and Redis to compose stack
```

---

*Next: [Database Schema Design](../database/schema-design.md)*
