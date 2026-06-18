# ResOS — API Contracts

> **Phase 1 Deliverable** | REST API Specification  
> **Status:** Awaiting Review  
> **Base URL:** `/api/v1`  
> **Content-Type:** `application/json`

---

## 1. API Conventions

### 1.1 General Rules

| Convention | Standard |
|------------|----------|
| Base path | `/api/v1` |
| Authentication | `Authorization: Bearer {accessToken}` |
| Tenant header | `X-Tenant-ID: {tenantUuid}` (required for tenant-scoped endpoints) |
| Pagination | `?page=0&size=20&sort=createdAt,desc` |
| Filtering | `?status=ACTIVE&category=Produce` |
| Search | `?search=tomato` |
| Idempotency | `Idempotency-Key: {uuid}` (for POST on orders, payments) |
| API versioning | URL path (`/v1/`) |

### 1.2 Standard Response Envelope

**Success (single resource):**
```json
{
  "data": { ... },
  "meta": {
    "timestamp": "2026-06-18T10:30:00Z"
  }
}
```

**Success (paginated list):**
```json
{
  "data": [ ... ],
  "meta": {
    "page": 0,
    "size": 20,
    "totalElements": 142,
    "totalPages": 8,
    "timestamp": "2026-06-18T10:30:00Z"
  }
}
```

**Error:**
```json
{
  "error": {
    "code": "TENANT_ACCESS_DENIED",
    "message": "You do not have access to this tenant's resources.",
    "details": [],
    "timestamp": "2026-06-18T10:30:00Z",
    "path": "/api/v1/inventory"
  }
}
```

### 1.3 HTTP Status Codes

| Code | Usage |
|------|-------|
| 200 | Success (GET, PUT, PATCH) |
| 201 | Created (POST) |
| 204 | No Content (DELETE) |
| 400 | Validation error |
| 401 | Unauthenticated |
| 403 | Forbidden (RBAC or tenant isolation) |
| 404 | Resource not found |
| 409 | Conflict (duplicate, optimistic lock) |
| 422 | Business rule violation |
| 429 | Rate limit exceeded |
| 500 | Internal server error |

### 1.4 Error Codes

| Code | HTTP | Description |
|------|------|-------------|
| `VALIDATION_ERROR` | 400 | Request body validation failed |
| `UNAUTHENTICATED` | 401 | Missing or invalid token |
| `TOKEN_EXPIRED` | 401 | Access token expired |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `TENANT_ACCESS_DENIED` | 403 | Cross-tenant access attempt |
| `TENANT_MISMATCH` | 403 | X-Tenant-ID doesn't match JWT |
| `NOT_FOUND` | 404 | Resource not found |
| `DUPLICATE_RESOURCE` | 409 | Unique constraint violation |
| `OPTIMISTIC_LOCK` | 409 | Concurrent modification conflict |
| `BUSINESS_RULE_VIOLATION` | 422 | Domain rule failed |
| `PLAN_LIMIT_EXCEEDED` | 422 | Subscription plan limit reached |

---

## 2. Authentication APIs (Phase 2)

### POST `/auth/login`

Authenticate user and receive tokens.

**Request:**
```json
{
  "email": "owner@joespizza.com",
  "password": "SecurePass123!",
  "tenantSlug": "joes-pizza"
}
```

**Response `200`:**
```json
{
  "data": {
    "accessToken": "eyJhbG...",
    "expiresIn": 900,
    "tokenType": "Bearer",
    "user": {
      "id": "uuid",
      "email": "owner@joespizza.com",
      "firstName": "Joe",
      "lastName": "Smith",
      "roles": ["TENANT_OWNER"],
      "permissions": ["inventory:read", "inventory:write", "orders:read", "..."]
    },
    "tenant": {
      "id": "uuid",
      "name": "Joe's Pizza",
      "slug": "joes-pizza"
    }
  }
}
```

**Set-Cookie:** `refreshToken=...; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth`

---

### POST `/auth/refresh`

Rotate refresh token and issue new access token.

**Request:** Cookie-based (refreshToken) OR body:
```json
{
  "refreshToken": "uuid"
}
```

**Response `200`:** Same shape as login (new accessToken + rotated refreshToken cookie).

---

### POST `/auth/logout`

Revoke refresh token.

**Headers:** `Authorization: Bearer {accessToken}`

**Response `204`:** No content. Clears refreshToken cookie.

---

### POST `/auth/register`

Register new tenant + owner account.

**Request:**
```json
{
  "tenantName": "Joe's Pizza",
  "tenantSlug": "joes-pizza",
  "email": "owner@joespizza.com",
  "password": "SecurePass123!",
  "firstName": "Joe",
  "lastName": "Smith",
  "phone": "+1234567890"
}
```

**Response `201`:** Same as login response.

---

### GET `/auth/me`

Get current authenticated user profile.

**Headers:** `Authorization: Bearer {accessToken}`

**Response `200`:**
```json
{
  "data": {
    "id": "uuid",
    "email": "owner@joespizza.com",
    "firstName": "Joe",
    "lastName": "Smith",
    "roles": ["TENANT_OWNER"],
    "permissions": ["..."],
    "tenant": { "id": "uuid", "name": "Joe's Pizza", "slug": "joes-pizza" }
  }
}
```

---

## 3. Tenant APIs (Phase 3)

### GET `/tenants/current`

Get current tenant details.

**Permissions:** Any authenticated tenant user

**Response `200`:**
```json
{
  "data": {
    "id": "uuid",
    "name": "Joe's Pizza",
    "slug": "joes-pizza",
    "email": "contact@joespizza.com",
    "phone": "+1234567890",
    "timezone": "America/New_York",
    "currency": "USD",
    "status": "ACTIVE",
    "settings": {},
    "subscription": {
      "plan": "PRO",
      "status": "ACTIVE",
      "currentPeriodEnd": "2026-07-18T00:00:00Z"
    }
  }
}
```

---

### PUT `/tenants/current`

Update tenant settings.

**Permissions:** `settings:write`

**Request:**
```json
{
  "name": "Joe's Pizza & Grill",
  "phone": "+1234567890",
  "timezone": "America/New_York",
  "settings": { "autoAcceptOrders": true }
}
```

---

### POST `/admin/tenants` (Platform Admin)

Create tenant (SUPER_ADMIN only).

**Permissions:** `tenant:manage`

---

## 4. User Management APIs (Phase 2)

### GET `/users`

List tenant users.

**Permissions:** `settings:read`

**Query:** `?page=0&size=20&status=ACTIVE&role=STAFF`

**Response `200`:** Paginated list of users.

---

### POST `/users`

Invite/create user in tenant.

**Permissions:** `settings:write`

**Request:**
```json
{
  "email": "manager@joespizza.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "role": "MANAGER",
  "password": "TempPass123!"
}
```

---

### GET `/users/{id}`

**Permissions:** `settings:read` OR self

---

### PUT `/users/{id}`

Update user profile/role.

**Permissions:** `settings:write`

---

### DELETE `/users/{id}`

Soft-delete user.

**Permissions:** `settings:write`

---

## 5. Restaurant APIs (Phase 4)

### GET `/restaurants`

List tenant restaurants/locations.

**Permissions:** Any authenticated tenant user

---

### POST `/restaurants`

**Permissions:** `settings:write`

**Request:**
```json
{
  "name": "Joe's Pizza - Downtown",
  "address": "123 Main St, New York, NY 10001",
  "phone": "+1234567890",
  "capacity": 80,
  "openingHours": {
    "monday": { "open": "11:00", "close": "23:00" },
    "tuesday": { "open": "11:00", "close": "23:00" }
  }
}
```

---

### GET `/restaurants/{id}`

### PUT `/restaurants/{id}`

### DELETE `/restaurants/{id}`

---

## 6. Dashboard APIs (Phase 4)

### GET `/dashboard/kpis`

Aggregated KPIs for dashboard widgets.

**Permissions:** Any authenticated tenant user

**Query:** `?restaurantId={uuid}&period=TODAY|WEEK|MONTH`

**Response `200`:**
```json
{
  "data": {
    "revenue": { "value": 4250.00, "change": 12.5, "trend": "UP" },
    "orders": { "value": 87, "change": -3.2, "trend": "DOWN" },
    "reservations": { "value": 24, "change": 8.0, "trend": "UP" },
    "lowStockItems": { "value": 5, "change": 0, "trend": "FLAT" },
    "activeEmployees": { "value": 12, "change": 0, "trend": "FLAT" },
    "avgOrderValue": { "value": 48.85, "change": 5.1, "trend": "UP" }
  }
}
```

---

### GET `/dashboard/recent-orders`

**Query:** `?restaurantId={uuid}&limit=10`

---

### GET `/dashboard/revenue-chart`

**Query:** `?restaurantId={uuid}&period=WEEK&groupBy=DAY`

**Response `200`:**
```json
{
  "data": {
    "labels": ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
    "values": [1200, 980, 1450, 1100, 1800, 2200, 1950]
  }
}
```

---

## 7. Inventory APIs (Phase 5)

### GET `/inventory`

List inventory items.

**Permissions:** `inventory:read`

**Query:** `?page=0&size=20&category=Produce&lowStock=true&search=tomato&restaurantId={uuid}`

**Response item shape:**
```json
{
  "id": "uuid",
  "name": "Tomatoes",
  "sku": "PROD-001",
  "category": "Produce",
  "unit": "kg",
  "currentStock": 2.5,
  "minimumStock": 5.0,
  "maximumStock": 50.0,
  "unitCost": 3.50,
  "supplier": "Fresh Farms Co.",
  "expiryDate": "2026-06-25",
  "isLowStock": true,
  "restaurantId": "uuid",
  "createdAt": "2026-01-15T08:00:00Z",
  "updatedAt": "2026-06-18T06:00:00Z",
  "version": 3
}
```

---

### POST `/inventory`

Create inventory item.

**Permissions:** `inventory:write`

**Request:**
```json
{
  "restaurantId": "uuid",
  "name": "Tomatoes",
  "sku": "PROD-001",
  "category": "Produce",
  "unit": "kg",
  "currentStock": 25.0,
  "minimumStock": 5.0,
  "maximumStock": 50.0,
  "unitCost": 3.50,
  "supplier": "Fresh Farms Co."
}
```

---

### GET `/inventory/{id}`

### PUT `/inventory/{id}`

**Headers:** Include `If-Match: {version}` for optimistic locking (optional).

---

### DELETE `/inventory/{id}`

Soft delete.

**Permissions:** `inventory:delete`

---

### POST `/inventory/{id}/transactions`

Record stock movement.

**Permissions:** `inventory:write`

**Request:**
```json
{
  "type": "PURCHASE",
  "quantity": 10.0,
  "unitCost": 3.50,
  "reference": "PO-2026-0142",
  "notes": "Weekly produce delivery"
}
```

**Types:** `PURCHASE`, `USAGE`, `WASTE`, `ADJUSTMENT`, `TRANSFER`

---

### GET `/inventory/{id}/transactions`

Transaction history for an item.

---

### GET `/inventory/alerts`

List active stock alerts.

**Permissions:** `inventory:read`

**Query:** `?acknowledged=false&restaurantId={uuid}`

---

### PATCH `/inventory/alerts/{id}/acknowledge`

**Permissions:** `inventory:write`

---

## 8. Employee APIs (Phase 6)

### GET `/employees`

**Permissions:** `employees:read`

**Query:** `?restaurantId={uuid}&status=ACTIVE&position=Server`

---

### POST `/employees`

**Permissions:** `employees:write`

**Request:**
```json
{
  "restaurantId": "uuid",
  "firstName": "Mike",
  "lastName": "Johnson",
  "email": "mike@joespizza.com",
  "phone": "+1234567890",
  "position": "Server",
  "hourlyRate": 15.50,
  "hireDate": "2026-03-01"
}
```

---

### GET `/employees/{id}`

### PUT `/employees/{id}`

### DELETE `/employees/{id}`

---

### GET `/employees/{id}/schedules`

**Query:** `?startDate=2026-06-01&endDate=2026-06-30`

---

### POST `/employees/{id}/schedules`

**Request:**
```json
{
  "restaurantId": "uuid",
  "shiftDate": "2026-06-20",
  "startTime": "09:00",
  "endTime": "17:00",
  "notes": "Opening shift"
}
```

---

### PUT `/employees/schedules/{scheduleId}`

### DELETE `/employees/schedules/{scheduleId}`

---

## 9. Reservation APIs (Phase 7)

### GET `/reservations`

**Permissions:** `reservations:read`

**Query:** `?restaurantId={uuid}&date=2026-06-20&status=CONFIRMED`

---

### POST `/reservations`

**Permissions:** `reservations:write`

**Request:**
```json
{
  "restaurantId": "uuid",
  "tableId": "uuid",
  "guestName": "John Doe",
  "guestPhone": "+1234567890",
  "guestEmail": "john@example.com",
  "partySize": 4,
  "reservationDate": "2026-06-20",
  "startTime": "19:00",
  "specialRequests": "Window seat preferred"
}
```

---

### GET `/reservations/{id}`

### PUT `/reservations/{id}`

### PATCH `/reservations/{id}/status`

**Request:**
```json
{
  "status": "SEATED"
}
```

**Valid transitions:**
```
PENDING → CONFIRMED → SEATED → COMPLETED
         ↘ CANCELLED
         ↘ NO_SHOW
```

---

### DELETE `/reservations/{id}`

Cancel reservation (sets status to CANCELLED).

---

### GET `/tables`

List restaurant tables.

**Query:** `?restaurantId={uuid}`

---

### POST `/tables`

### PUT `/tables/{id}`

### DELETE `/tables/{id}`

---

### GET `/reservations/availability`

Check table availability.

**Query:** `?restaurantId={uuid}&date=2026-06-20&partySize=4&startTime=19:00`

**Response `200`:**
```json
{
  "data": {
    "available": true,
    "suggestedTables": [
      { "id": "uuid", "tableNumber": "T12", "capacity": 4 }
    ]
  }
}
```

---

## 10. Menu APIs (Phase 8)

### GET `/menu/categories`

**Permissions:** `menu:read`

**Query:** `?restaurantId={uuid}`

---

### POST `/menu/categories`

**Permissions:** `menu:write`

---

### GET `/menu/items`

**Query:** `?restaurantId={uuid}&categoryId={uuid}&available=true`

---

### POST `/menu/items`

**Request:**
```json
{
  "categoryId": "uuid",
  "name": "Margherita Pizza",
  "description": "Fresh mozzarella, tomato sauce, basil",
  "price": 14.99,
  "cost": 4.50,
  "preparationTime": 15,
  "allergens": ["gluten", "dairy"],
  "modifiers": [
    { "name": "Extra Cheese", "priceAdjustment": 2.00 },
    { "name": "Gluten-Free Crust", "priceAdjustment": 3.00 }
  ]
}
```

---

### GET `/menu/items/{id}`

### PUT `/menu/items/{id}`

### DELETE `/menu/items/{id}`

### PATCH `/menu/items/{id}/availability`

**Request:** `{ "isAvailable": false }`

---

## 11. Order APIs (Phase 8)

### GET `/orders`

**Permissions:** `orders:read`

**Query:** `?restaurantId={uuid}&status=PREPARING&date=2026-06-18`

---

### POST `/orders`

**Permissions:** `orders:write`

**Request:**
```json
{
  "restaurantId": "uuid",
  "tableId": "uuid",
  "orderType": "DINE_IN",
  "customerName": "Table 12",
  "items": [
    {
      "menuItemId": "uuid",
      "quantity": 2,
      "modifiers": [{ "name": "Extra Cheese", "priceAdjustment": 2.00 }],
      "specialInstructions": "Well done"
    }
  ],
  "notes": "Birthday celebration"
}
```

---

### GET `/orders/{id}`

Includes items and status history.

---

### PATCH `/orders/{id}/status`

**Request:**
```json
{
  "status": "PREPARING",
  "notes": "Sent to kitchen"
}
```

**Valid transitions:**
```
PENDING → CONFIRMED → PREPARING → READY → SERVED → COMPLETED
                                              ↘ CANCELLED (any active state)
```

---

### PATCH `/orders/{id}/items/{itemId}/status`

Update individual item status (kitchen display).

---

## 12. Analytics APIs (Phase 9)

### GET `/analytics/revenue`

**Permissions:** `analytics:read`

**Query:** `?restaurantId={uuid}&startDate=2026-06-01&endDate=2026-06-30&groupBy=DAY`

---

### GET `/analytics/inventory`

Inventory turnover, waste analysis, cost trends.

**Query:** `?restaurantId={uuid}&period=MONTH`

---

### GET `/analytics/employees`

Labor cost, hours worked, productivity metrics.

**Query:** `?restaurantId={uuid&startDate=2026-06-01&endDate=2026-06-30`

---

### GET `/analytics/orders`

Order volume, average ticket, peak hours.

---

## 13. Notification APIs

### GET `/notifications`

**Query:** `?unread=true&page=0&size=20`

---

### PATCH `/notifications/{id}/read`

---

### PATCH `/notifications/read-all`

Mark all as read.

---

### GET `/notifications/unread-count`

**Response:**
```json
{ "data": { "count": 5 } }
```

---

## 14. Audit Log APIs

### GET `/audit-logs`

**Permissions:** `settings:read` (Owner/Manager only)

**Query:** `?entityType=InventoryItem&entityId={uuid}&startDate=2026-06-01&action=UPDATE`

---

## 15. Health & System

### GET `/actuator/health`

Public health check (no auth).

### GET `/actuator/info`

Application info.

---

## 16. WebSocket Events (Future)

| Event | Direction | Payload |
|-------|-----------|---------|
| `order.created` | Server → Client | Order object |
| `order.status.changed` | Server → Client | Order ID + new status |
| `stock.alert` | Server → Client | Alert object |
| `reservation.created` | Server → Client | Reservation object |
| `notification.new` | Server → Client | Notification object |

**Connection:** `wss://api.resos.com/ws?token={accessToken}&tenantId={uuid}`

---

## 17. Rate Limits

| Endpoint Group | Limit |
|----------------|-------|
| `/auth/login` | 5 req/min per IP |
| `/auth/register` | 3 req/min per IP |
| `/auth/refresh` | 10 req/min per user |
| All other endpoints | 100 req/min per user |
| `/admin/*` | 50 req/min per admin |

**Headers in response:**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1718706600
```

---

*Next: [Development Roadmap](../roadmap/development-roadmap.md)*
