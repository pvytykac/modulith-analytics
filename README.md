# modulith-analytics

A Spring Modulith application providing CRUD for applications/users/licenses/accounts with a star-schema analytics engine for license assignment tracking.

**Stack:** Java 25, Spring Boot 4.0.6, Spring Modulith 2.0.6, Spring Data JPA, PostgreSQL 18, Caffeine cache, MapStruct 1.6.3, Lombok 1.18.46, Testcontainers 1.21.4

---

## Module Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Application (core)                       │
│  exposes: api, events                                       │
│  depends: nothing                                           │
└────────────┬───────────────┬────────────────┬───────────────┘
             │               │                │
      listens│       uses/api│        uses/api│
             ▼               ▼                ▼
┌─────────────────┐ ┌─────────────────┐  ┌─────────────────────┐
│     License     │ │     Account     │  │      Analytics      │
│  exposes: api,  │ │  exposes: events│  │  (event sink)       │
│  events         │ │  depends:       │  │  depends:           │
│  depends:       │ │  - app api      │  │  - app events       │
│  - app events   │ │  - license api  │  │  - user events      │
│  - app api      │ │  - user events  │  │  - account events   │
└─────────────────┘ └─────────────────┘  │  - license events   │
       │                     │           │  - license api      │
       │                     │           └─────────────────────┘
       │                     │                       ▲
       │     listens(events) │          listens to   │
       └─────────────────────┼───────────────────────┘
                             │
┌──────────────────┐         │
│      User        │ listens │
│  exposes: events │─────────┘
│  depends: nothing│
└──────────────────┘
```

### Module Dependencies

| Module | `allowedDependencies` | Named Interfaces |
|---|---|---|
| `application` | *(none)* | `api`, `events` |
| `user` | *(none)* | `events` |
| `license` | `application::events`, `application::api` | `api`, `events` |
| `account` | `application::events`, `application::api`, `user::events`, `license::api` | `events` |
| `analytics` | `application::events`, `user::events`, `account::events`, `license::events`, `license::api` | *(none)* |

---

## Analytics Package Deep Dive

Package: `net.pvytykac.modularapp.analytics`

The analytics module implements a **star-schema** data warehouse pattern: domain events drive four dimension tables and one fact table, which power two reporting endpoints.

### Event Flow

```
Domain Events (published by other modules)
         │
         ▼
Dimension Event Handlers        Fact Event Handlers
(update dim_* tables)            (insert fact_* rows)
         │                              │
         ▼                              ▼
   dim_applications             fact_license_assignments
   dim_users                           ▲
   dim_accounts                        │
   dim_licenses              LicenseAssignmentFact
                                    (ASSIGNED/REVOKED)
```

### Dimension Tables

All dimensions use `@Builder.Default boolean active = true` for soft-delete. When a source entity is deleted, the corresponding dimension row is deactivated rather than removed.

#### `dim_applications` — `ApplicationDimension`

| Field | Type | Source |
|---|---|---|
| `id` | UUID (PK) | auto-generated |
| `applicationId` | `String` | `ApplicationCreatedEvent.applicationId` |
| `name` | `String` | `ApplicationCreatedEvent.name` (updated via `ApplicationUpdatedEvent`) |
| `createdAt` | `Instant` | `ApplicationCreatedEvent.createdAt` |
| `active` | `boolean` | `true` by default; set to `false` on `ApplicationDeletedEvent` |

Event handlers: `ApplicationDimensionEventHandler`

#### `dim_users` — `UserDimension`

| Field | Type | Source |
|---|---|---|
| `id` | UUID (PK) | auto-generated |
| `userId` | `String` | `UserCreatedEvent.userId` |
| `displayName` | `String` | `UserCreatedEvent.displayName` (updated via `UserUpdatedEvent`) |
| `email` | `String` | `UserCreatedEvent.email` (updated via `UserUpdatedEvent`) |
| `active` | `boolean` | `true` by default; set to `false` on `UserDeletedEvent` |

Event handlers: `UserDimensionEventHandlers`

#### `dim_licenses` — `LicenseDimension`

| Field | Type | Source |
|---|---|---|
| `id` | UUID (PK) | auto-generated |
| `licenseId` | `String` | `LicenseCreatedEvent.licenseId` |
| `applicationId` | `String` | `LicenseCreatedEvent.applicationId` |
| `name` | `String` | `LicenseCreatedEvent.name` (updated via `LicenseUpdatedEvent`) |
| `cost` | `BigDecimal` | `LicenseCreatedEvent.monthlyPrice` (updated via `LicenseUpdatedEvent`) |
| `active` | `boolean` | `true` by default; set to `false` on `LicenseDeletedEvent` / `ApplicationDeletedEvent` |

Event handlers: `LicenseDimensionEventHandlers`

#### `dim_accounts` — `AccountDimension`

| Field | Type | Source |
|---|---|---|
| `id` | UUID (PK) | auto-generated |
| `accountId` | `String` | `AccountCreatedEvent.accountId` |
| `applicationId` | `String` | `AccountCreatedEvent.applicationId` |
| `userId` | `String` | `AccountCreatedEvent.userId` |
| `lastUsed` | `Instant` | `AccountCreatedEvent.lastUsed` (updated via `AccountUpdatedEvent`) |
| `active` | `boolean` | `true` by default; set to `false` on `AccountDeletedEvent` / `ApplicationDeletedEvent` / `UserDeletedEvent` |

Event handlers: `AccountDimensionEventHandler`

### Fact Table

#### `fact_license_assignments` — `LicenseAssignmentFact`

| Field | Type | Description |
|---|---|---|
| `id` | UUID (PK) | auto-generated |
| `licenseDimensionKey` | `String` | FK → `dim_licenses.id` |
| `accountDimensionKey` | `String` | FK → `dim_accounts.id` |
| `userDimensionKey` | `String` | FK → `dim_users.id` |
| `applicationDimensionKey` | `String` | FK → `dim_applications.id` |
| `licenseMonthlyPrice` | `BigDecimal` | Snapshot of license price at time of assignment |
| `eventType` | `enum` (ASSIGNED / REVOKED) | Whether the license was granted or removed |
| `assignmentTime` | `Instant` | When the assignment change occurred |

The fact table is denormalized (monthly price stored directly) to preserve historical accuracy — price changes after the fact don't retroactively alter past records.

Event handlers: `LicenseAssignmentEventHandler`

- **ASSIGNED** rows created on: `AccountCreatedEvent` (all licenses), `AccountUpdatedEvent` (newly assigned licenses)
- **REVOKED** rows created on: `AccountUpdatedEvent` (removed licenses), `AccountDeletedEvent` (all licenses)

### Reports

Controller: `ApplicationAnalyticsController` at `/v1/applications/{applicationId}/analytics/licenseAssignments`

Both endpoints accept the same rich filter set and operate by joining the fact table with all four dimensions:

| Filter Parameter | Type | Matches Against |
|---|---|---|
| `startTime` | `Instant` (required) | `fact.assignmentTime` |
| `endTime` | `Instant` (required) | `fact.assignmentTime` |
| `accountId` | `String` | `dim_accounts.accountId` |
| `userId` | `String` | `dim_users.userId` |
| `licenseId` | `String` | `dim_licenses.licenseId` |
| `email` | `String` | `dim_users.email` |
| `minLicenseCost` / `maxLicenseCost` | `BigDecimal` | `dim_licenses.cost` |
| `minApplicationCreatedAt` / `maxApplicationCreatedAt` | `Instant` | `dim_applications.createdAt` |
| `minLastUsed` / `maxLastUsed` | `Instant` | `dim_accounts.lastUsed` |

#### `GET .../dailyBreakdown`

Returns a time-series grouped by date:

```json
{
  "startTime": "...",
  "endTime": "...",
  "applicationId": "...",
  "content": [
    {
      "date": "2024-03-02",
      "assignedCount": 1,
      "assignedCost": 10.00,
      "revokedCount": 0,
      "revokedCost": 0.00
    }
  ]
}
```

- Repository: `LicenseAssignmentRepository.getDailyLicenseAssignmentBreakdown()`
- DTO: `LicenseAssignmentBreakdown` (`@Data`)

#### `GET .../statistics`

Returns per-license aggregation with pagination:

```json
{
  "content": [
    {
      "id": "lic-xyz",
      "name": "Starter",
      "monthlyCost": 10.00,
      "assignedCount": 5,
      "revokedCount": 2
    }
  ],
  "page": { ... }
}
```

- Repository: `LicenseAssignmentRepository.getLicenseAssignmentStatistics()`
- DTO: `LicenseAssignmentStatistics` (`@Data`)

---

A Postman collection is available at [`analytics.postman_collection.json`](analytics.postman_collection.json) for API exploration.
