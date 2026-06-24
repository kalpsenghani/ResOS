# ResOS — Production Deployment Guide

> **Phase 10 Deliverable** | Operations Runbook  
> **Audience:** DevOps / Platform Engineers

---

## 1. Overview

ResOS ships as two container images plus managed PostgreSQL and Redis:

| Component | Image / Service | Port |
|-----------|-----------------|------|
| Angular SPA + Nginx | `resos-frontend` | 80 |
| Spring Boot API | `resos-backend` | 8080 (internal) |
| PostgreSQL | `postgres:16-alpine` | 5432 |
| Redis | `redis:7-alpine` | 6379 |

The frontend Nginx reverse-proxies `/api/` and `/actuator/` to the backend. The Angular production build uses a relative API base (`/api/v1`), so browser requests stay same-origin.

---

## 2. Prerequisites

- Docker 24+ and Docker Compose v2
- OpenSSL (for JWT key generation)
- Java 21 and Node 20 (only if building outside Docker)

---

## 3. Local Production Stack (Docker Compose)

### 3.1 Generate JWT keys

```powershell
.\scripts\generate-jwt-keys.ps1
```

```bash
./scripts/generate-jwt-keys.sh
```

### 3.2 Configure environment

```powershell
Copy-Item .env.example .env
# Edit .env — set POSTGRES_PASSWORD, SPRING_DATASOURCE_PASSWORD,
# RESOS_JWT_PRIVATE_KEY, RESOS_JWT_PUBLIC_KEY, RESOS_CORS_ALLOWED_ORIGINS
```

For PEM values in `.env`, paste the key contents on one line or store keys in your secrets manager and inject at deploy time.

### 3.3 Start the stack

```powershell
docker compose -f docker-compose.prod.yml up -d --build
```

| URL | Description |
|-----|-------------|
| `http://localhost` | Frontend (default `FRONTEND_PORT=80`) |
| `http://localhost/api/v1/...` | API (proxied via Nginx) |
| `http://localhost/actuator/health` | Backend health check |

### 3.4 Verify

```powershell
curl http://localhost/actuator/health
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs backend --tail 50
```

---

## 4. Development Environments

### Infrastructure only (recommended for daily dev)

Start PostgreSQL and Redis, run backend/frontend on the host:

```powershell
docker compose up -d
cd backend; .\mvnw.cmd spring-boot:run
cd ..\frontend; npm start
```

### Full containerized dev stack

Uses the `dev` Spring profile with ephemeral JWT keys:

```powershell
docker compose -f docker-compose.dev.yml up -d --build
```

Frontend: `http://localhost:8081` · Backend (direct): `http://localhost:8080`

---

## 5. CI/CD

### GitHub Actions (CI)

Workflow: `.github/workflows/ci.yml`

On push/PR to `develop`, `main`, or `feature/**`:

1. Backend — `./mvnw test`
2. Frontend — `npm ci`, production build, Karma unit tests
3. Docker — build both images and validate `docker-compose.prod.yml`

### Jenkins (CD)

Pipeline: `jenkins/Jenkinsfile`

| Stage | Action |
|-------|--------|
| Checkout | Clone repository |
| Backend Tests | Maven test suite |
| Frontend Tests | Build + unit tests |
| Build Images | Tag with short Git SHA |
| Push Images | `main` / `develop` only — requires `docker-registry-credentials` |
| Deploy | Manual approval on `main` |

Configure Jenkins credentials:

- `docker-registry-credentials` — registry username/password or token
- `DOCKER_REGISTRY` — e.g. `ghcr.io/your-org`

Replace the Deploy stage shell block with your orchestrator commands (ECS service update, Kubernetes rollout, etc.).

---

## 6. Production Checklist

```
□ Strong PostgreSQL password (unique per environment)
□ JWT RSA key pair stored in secrets manager (never in git)
□ RESOS_CORS_ALLOWED_ORIGINS set to production frontend URL(s)
□ TLS termination at load balancer or ingress
□ Database backups enabled (RDS automated backups or pg_dump cron)
□ Redis persistence configured (AOF enabled in compose prod)
□ Log aggregation (CloudWatch, ELK, or equivalent)
□ Health checks wired to load balancer
□ Rate limiting / WAF at edge (recommended)
```

---

## 7. Environment Variables

| Variable | Required (prod) | Description |
|----------|-----------------|-------------|
| `POSTGRES_PASSWORD` | Yes | PostgreSQL password |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Backend DB password (match Postgres) |
| `RESOS_JWT_PRIVATE_KEY` | Yes | RSA private key PEM |
| `RESOS_JWT_PUBLIC_KEY` | Yes | RSA public key PEM |
| `RESOS_CORS_ALLOWED_ORIGINS` | Yes | Comma-separated allowed origins |
| `FRONTEND_PORT` | No | Host port for Nginx (default `80`) |
| `REDIS_HOST` | No | Redis hostname (default `redis`) |

---

## 8. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Backend exits on startup | Missing JWT keys | Set `RESOS_JWT_*` or use dev compose |
| 502 from Nginx | Backend not healthy | Check `docker compose logs backend` |
| CORS errors | Origin mismatch | Update `RESOS_CORS_ALLOWED_ORIGINS` |
| Flyway migration failure | Schema drift | Restore DB snapshot; run migrations on staging first |
| Integration tests fail locally | Docker Desktop stopped | Start Docker Desktop before `mvnw test` |

---

## 9. Related Documentation

- [AWS Architecture](03-aws-architecture.md)
- [System Overview](01-system-overview.md)
- [Development Roadmap](../roadmap/development-roadmap.md)
