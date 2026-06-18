# ResOS — System Architecture Overview

> **Phase 1 Deliverable** | Multi-Tenant Restaurant Management SaaS  
> **Status:** Awaiting Review  
> **Version:** 1.0.0

---

## 1. Executive Summary

ResOS is a production-grade, multi-tenant SaaS platform for restaurant operations. Each restaurant (tenant) operates in complete data isolation while sharing a common application infrastructure. The system follows **Clean Architecture**, **Domain-Driven Design (DDD)**, and **SOLID** principles.

### Target Users

| Role | Scope | Primary Capabilities |
|------|-------|---------------------|
| **System Administrator** | Platform-wide | Tenant provisioning, billing oversight, platform analytics |
| **Restaurant Owner** | Single tenant | Full tenant control, billing, settings, all modules |
| **Restaurant Manager** | Single tenant | Operations, staff, inventory, reservations, orders |
| **Staff Member** | Single tenant | Orders, reservations (limited), assigned tasks |

---

## 2. High-Level Architecture Diagram

```mermaid
flowchart TB
    subgraph Clients["Client Layer"]
        WEB["Angular 20 SPA<br/>(Standalone Components)"]
        MOB["Future: Mobile / Tablet PWA"]
    end

    subgraph Edge["Edge Layer"]
        CDN["CloudFront CDN"]
        ALB["AWS ALB / Nginx"]
    end

    subgraph App["Application Layer"]
        API["Spring Boot 3 API<br/>(Java 21)"]
        AUTH["Auth Service<br/>(JWT + Refresh)"]
        TENANT["Tenant Resolver<br/>(Context + Filters)"]
    end

    subgraph Data["Data Layer"]
        PG[("PostgreSQL<br/>Shared DB + tenant_id")]
        REDIS[("Redis<br/>Cache + Sessions")]
    end

    subgraph Async["Async Layer (Future)"]
        QUEUE["SQS / RabbitMQ"]
        WORKER["Notification Worker"]
    end

    subgraph Observability["Observability"]
        LOGS["CloudWatch / ELK"]
        METRICS["Prometheus / Grafana"]
        AUDIT["Audit Log Pipeline"]
    end

    WEB --> CDN --> ALB --> API
    MOB --> CDN
    API --> AUTH
    API --> TENANT
    API --> PG
    API --> REDIS
    API --> QUEUE
    QUEUE --> WORKER
    API --> LOGS
    API --> METRICS
    API --> AUDIT
```

---

## 3. Layered Architecture (Backend)

```mermaid
flowchart LR
    subgraph Presentation["Presentation Layer"]
        CTRL["REST Controllers"]
        DTO["Request/Response DTOs"]
        VAL["Validation + Exception Handlers"]
    end

    subgraph Application["Application Layer"]
        SVC["Application Services"]
        MAP["Mappers"]
        EVT["Domain Events"]
    end

    subgraph Domain["Domain Layer"]
        ENT["Entities / Aggregates"]
        VO["Value Objects"]
        REPO_INT["Repository Interfaces"]
        DOM_SVC["Domain Services"]
    end

    subgraph Infrastructure["Infrastructure Layer"]
        JPA["JPA Repositories"]
        SEC["Spring Security"]
        CACHE["Redis Cache"]
        EXT["External Integrations"]
    end

    CTRL --> SVC
    SVC --> ENT
    SVC --> REPO_INT
    REPO_INT --> JPA
    SEC --> CTRL
    CACHE --> SVC
```

### Layer Responsibilities

| Layer | Responsibility | Dependencies |
|-------|---------------|--------------|
| **Presentation** | HTTP handling, DTO mapping, input validation | Application |
| **Application** | Use cases, orchestration, transactions | Domain |
| **Domain** | Business rules, entities, invariants | None (pure) |
| **Infrastructure** | Persistence, security, caching, messaging | Domain interfaces |

---

## 4. Frontend Architecture

```mermaid
flowchart TB
    subgraph Shell["App Shell"]
        ROUTER["Angular Router"]
        LAYOUT["Dashboard Layout"]
        THEME["Theme Service<br/>(Light / Dark)"]
    end

    subgraph State["State Management (NgRx)"]
        AUTH_STORE["Auth Store"]
        TENANT_STORE["Tenant Store"]
        UI_STORE["UI Store"]
        EFFECTS["Effects"]
    end

    subgraph Features["Feature Modules"]
        DASH["Dashboard"]
        INV["Inventory"]
        EMP["Employees"]
        RES["Reservations"]
        MENU["Menu & Orders"]
        ANAL["Analytics"]
    end

    subgraph Shared["Shared Layer"]
        UI_KIT["UI Component Library"]
        CORE["Core Services<br/>(HTTP, Interceptors)"]
        GUARDS["Route Guards"]
    end

    ROUTER --> LAYOUT
    LAYOUT --> Features
    Features --> State
    Features --> Shared
    EFFECTS --> CORE
    GUARDS --> AUTH_STORE
    GUARDS --> TENANT_STORE
```

---

## 5. Multi-Tenant Architecture Decision

### Chosen Strategy: **Shared Database, Shared Schema with Discriminator Column**

```
┌─────────────────────────────────────────────────────────┐
│                    PostgreSQL                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │  Tenant A   │  │  Tenant B   │  │  Tenant C   │     │
│  │ tenant_id=A │  │ tenant_id=B │  │ tenant_id=C │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
│         All rows in shared tables, filtered by tenant_id│
└─────────────────────────────────────────────────────────┘
```

### Why This Approach?

| Criteria | Shared DB + tenant_id | Schema-per-Tenant | DB-per-Tenant |
|----------|----------------------|-------------------|---------------|
| Cost efficiency | ✅ High | ⚠️ Medium | ❌ Low |
| Operational complexity | ✅ Low | ⚠️ Medium | ❌ High |
| Tenant isolation | ⚠️ Application-enforced | ✅ Strong | ✅ Strongest |
| Cross-tenant analytics | ✅ Easy | ❌ Hard | ❌ Hard |
| Migration management | ✅ Single schema | ⚠️ N schemas | ❌ N databases |
| Startup SaaS fit | ✅ Best | ⚠️ Overkill early | ❌ Enterprise only |

### Tenant Isolation Enforcement (Defense in Depth)

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant Filter as TenantFilter
    participant Security as Spring Security
    participant Service as Application Service
    participant Repo as JPA Repository
    participant DB as PostgreSQL

    Client->>Gateway: Request + JWT + X-Tenant-ID
    Gateway->>Security: Validate JWT
    Security->>Filter: Extract tenant from JWT claim
    Filter->>Filter: Validate X-Tenant-ID matches JWT
    Filter->>Filter: Set TenantContext (ThreadLocal)
    Filter->>Service: Proceed with request
    Service->>Repo: findById(id)
    Repo->>Repo: Auto-append WHERE tenant_id = ?
    Repo->>DB: Parameterized query
    DB-->>Client: Tenant-scoped response
```

**Isolation Layers:**

1. **JWT Claim** — `tenant_id` embedded in access token
2. **Request Header** — `X-Tenant-ID` must match JWT claim
3. **TenantContext** — ThreadLocal holder set by servlet filter
4. **Hibernate Filter** — `@Filter(name = "tenantFilter")` on all tenant-scoped entities
5. **Repository Base** — `TenantAwareRepository` auto-applies tenant predicate
6. **Integration Tests** — Cross-tenant access attempts must return 403/404

---

## 6. Authentication Strategy

### Token Flow

```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Redis
    participant DB

    Client->>API: POST /auth/login (email, password, tenantSlug)
    API->>DB: Validate credentials + tenant membership
    API->>Redis: Store refresh token (jti → userId, tenantId)
    API-->>Client: accessToken (15min) + refreshToken (7d)

    Note over Client: Access token expires

    Client->>API: POST /auth/refresh (refreshToken)
    API->>Redis: Validate + rotate refresh token
    API-->>Client: New accessToken + refreshToken

    Client->>API: GET /api/v1/inventory (Bearer accessToken)
    API->>API: Validate JWT + extract tenant_id + roles
    API-->>Client: 200 OK (tenant-scoped data)
```

### Token Structure

| Token | Lifetime | Storage (Client) | Claims |
|-------|----------|------------------|--------|
| Access Token | 15 minutes | Memory (NgRx store) | `sub`, `tenant_id`, `roles[]`, `permissions[]`, `exp` |
| Refresh Token | 7 days | HttpOnly Secure Cookie | Opaque UUID, stored server-side in Redis |

### RBAC Model

```
User ──► UserRole ──► Role ──► RolePermission ──► Permission

Platform Roles (tenant_id = NULL):
  └── SUPER_ADMIN

Tenant Roles (scoped to tenant_id):
  ├── TENANT_OWNER    → All tenant permissions
  ├── MANAGER         → Operations, reports, staff (no billing)
  └── STAFF           → Orders, reservations (read), assigned tasks
```

---

## 7. State Management Strategy (Frontend)

| Store Slice | Contents | Persistence |
|-------------|----------|-------------|
| **auth** | user, accessToken, isAuthenticated, permissions | Session only |
| **tenant** | currentTenant, tenantSettings, slug | Session + localStorage (slug) |
| **ui** | theme (light/dark), sidebar state, loading | localStorage |
| **feature stores** | Per-feature entity caches (inventory, orders, etc.) | Memory (refetched on nav) |

**NgRx Effects** handle side effects: login, token refresh, tenant switch, API error normalization.

**Angular Signals** used for local component state (form state, UI toggles) where NgRx would be overkill.

---

## 8. Caching Strategy

| Cache Key Pattern | TTL | Purpose |
|-------------------|-----|---------|
| `tenant:{id}:settings` | 1 hour | Tenant configuration |
| `user:{id}:permissions` | 15 min | RBAC permission set |
| `menu:{tenantId}:active` | 30 min | Active menu for POS |
| `refresh:{jti}` | 7 days | Refresh token validation |
| `ratelimit:{ip}:{endpoint}` | 1 min | API rate limiting |

**Cache Invalidation:** Event-driven on writes (e.g., menu update → evict `menu:{tenantId}:*`).

---

## 9. Subscription & Billing Ready Architecture

Designed for future Stripe integration without refactoring core domain:

```
Tenant ──► Subscription ──► Plan ──► PlanFeature (limits)

Plan tiers (planned):
  STARTER   → 1 location, 5 staff, basic modules
  PRO       → 3 locations, 25 staff, analytics
  ENTERPRISE → Unlimited, API access, priority support
```

Feature flags enforced at **Application Service** layer via `@RequiresPlan("ANALYTICS")` annotation.

---

## 10. Testing Strategy

| Layer | Tool | Scope |
|-------|------|-------|
| Unit | JUnit 5 + Mockito | Domain logic, services, mappers |
| Integration | Spring Boot Test + Testcontainers | Repositories, API endpoints, tenant isolation |
| API Contract | REST Assured / Postman Collections | OpenAPI validation |
| Frontend Unit | Jasmine + Karma | Components, pipes, reducers |
| E2E | Cypress | Critical user flows per phase |
| Tenant Isolation | Dedicated test suite | Cross-tenant access MUST fail |
| Performance | k6 (Phase 10) | Load testing baseline |

---

## 11. DevOps Architecture (Planned — Phase 10)

```mermaid
flowchart LR
    DEV["Developer"] --> GH["GitHub"]
    GH --> GHA["GitHub Actions<br/>(CI: test, lint, build)"]
    GHA --> JENKINS["Jenkins<br/>(CD: deploy)"]
    JENKINS --> ECR["AWS ECR"]
    ECR --> ECS["AWS ECS / EKS"]
    ECS --> RDS["AWS RDS PostgreSQL"]
    ECS --> ELASTICACHE["AWS ElastiCache Redis"]
```

---

## 12. Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| API Response Time (p95) | < 200ms |
| Uptime SLA | 99.9% |
| Concurrent Tenants | 1,000+ |
| Data Retention (audit logs) | 7 years |
| Password Policy | BCrypt, min 12 chars |
| HTTPS | TLS 1.3 everywhere |
| CORS | Whitelist tenant domains |
| Rate Limiting | 100 req/min per user |

---

## 13. Security Checklist

- [ ] JWT signed with RS256 (asymmetric keys)
- [ ] Refresh token rotation on every use
- [ ] CSRF protection on cookie-based refresh
- [ ] SQL injection prevention (parameterized queries only)
- [ ] XSS prevention (Angular sanitization + CSP headers)
- [ ] Tenant isolation integration tests on every PR
- [ ] Audit log for all write operations
- [ ] Secrets in AWS Secrets Manager (not env files in prod)
- [ ] OWASP Top 10 review before Phase 10

---

*Next: Review this document, then proceed to [Folder Structure](./02-folder-structure.md)*
