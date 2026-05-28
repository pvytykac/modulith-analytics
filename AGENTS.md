# AGENTS.md — modulith-analytics

## Build & Test Commands

```bash
# Full build (unit + integration tests)
mvn clean verify

# Unit tests only (excludes *IntegrationTest)
mvn test

# Integration tests only (via failsafe)
mvn verify -Dit.test='*IntegrationTest'

# Single test class
mvn test -Dtest='net.pvytykac.modularapp.account.AccountModuleTest'

# Single test method
mvn test -Dtest='AccountModuleTest#createAndGetAccount'

# Modulith structure verification (in ModulithStructureTest)
mvn test -Dtest='ModulithStructureTest'

# Skip tests
mvn install -DskipTests

# Start dev database (PostgreSQL 18)
docker compose up -d
```

**Test infrastructure:** Uses Testcontainers (`PostgresTestContainerConfiguration`) to spin up a PostgreSQL container automatically. No need to start Docker Compose for tests.

---

## Project Overview

**Stack:** Java 25, Spring Boot 4.0.6, Spring Modulith 2.0.6, Spring Data JPA, MapStruct 1.6.3, Lombok 1.18.46, PostgreSQL, Caffeine cache, Testcontainers 1.21.4

**Package:** `net.pvytykac.modularapp`

**Modules** (Spring Modulith `@ApplicationModule`):
- `application` — core CRUD, exposes `api` and `events` named interfaces
- `user` — user CRUD, exposes `events`
- `license` — license CRUD + pricing API, exposes `api` and `events`
- `account` — account CRUD per application, exposes `events`
- `analytics` — star-schema analytics (dimensions + fact tables), listens to all module events

**Module dependencies are declared in each `package-info.java` via `@ApplicationModule(allowedDependencies = {...})`.**

---

## Code Style

### Visibility
- **Controllers, Services, Repositories, Mappers, Entities** inside `internal` packages are **package-private** (no `public` modifier). Only APIs and events in named interface packages (`api/`, `events/`) are `public`.
- Test classes are package-private.

### Imports
- Group by: `jakarta.*` → `lombok.*` → `org.springframework.*` → project imports → `java.*`
- No wildcard imports. No blank lines between import groups.
- `import static` comes last (used for `assertj` assertions).

### Types & Naming
- **Records for:** events (`*Event`), request/response DTOs (`PostRequest`, `PutRequest`), simple data carriers
- **`@Data` for:** statistics/analytics DTOs that need getters/setters/toString/equals/hashCode (e.g., `LicenseAssignmentBreakdown`, `LicenseAssignmentStatistics`)
- **Entities:** `*Entity` suffix, annotated `@Entity`, `@Getter` `@Setter` `@Builder` `@NoArgsConstructor` `@AllArgsConstructor`
- **Controllers:** `*Controller`, annotated `@RestController` + `@RequestMapping`, `@RequiredArgsConstructor`
- **Services:** `*Service`, `@Service` + `@RequiredArgsConstructor` + `@Slf4j`
- **Repositories:** `*Repository`, extends `JpaRepository`, `@Repository`
- **Mappers:** `*Mapper`, `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)`
- **Events:** `*Event` suffix, placed in `events/` subpackage
- **Test suffix:** `*Test` for unit/module tests, `*IntegrationTest` for integration tests (failsafe picks these up)
- Use `var` for local variable type inference consistently.

### Lombok
- `@RequiredArgsConstructor` on controllers/services (injects `final` fields)
- `@AllArgsConstructor` on event handlers
- `@Getter`/`@Setter` + `@Builder` + `@NoArgsConstructor`/`@AllArgsConstructor` on entities
- `@Builder.Default` for boolean fields defaulting to `true` (active flag)
- `@Slf4j` for logger (`log.debug(...)`, `log.info(...)`)

### MapStruct
- `componentModel = "spring"` and `unmappedTargetPolicy = ReportingPolicy.ERROR`
- Method naming: `entityToRepresentation(Entity entity)`

### Error Handling
- Global `@RestControllerAdvice` (`ExceptionResponseHandler`):
  - `NoSuchElementException` → 404 NOT_FOUND
  - `DuplicateKeyException` → 409 CONFLICT
- Services use `Optional.orElseThrow()` for "not found" cases (no custom exceptions needed).

### Data / JPA
- UUID primary keys: `@GeneratedValue(strategy = GenerationType.UUID)`
- `@Table` with explicit names (snake_case), `uniqueConstraints`, and `indexes`
- `@ElementCollection` for collections of simple types (e.g., `Set<String> licenseIds`)
- Pessimistic locking: `@Lock(LockModeType.PESSIMISTIC_WRITE)` with explicit `@Query` for "for update" reads
- `@Modifying` on bulk update/delete queries
- `@Transactional` at the class level on services and event handlers

### Validation
- Jakarta Bean Validation annotations on entity fields and request records: `@NotBlank`, `@NotNull`, `@Email`, `@PastOrPresent`, `@PositiveOrZero`
- Controllers use `@Validated` on `@RequestBody` parameters

### Caching
- `@EnableCaching` on `@Configuration` class
- `@Cacheable`, `@CachePut`, `@CacheEvict` on service methods with `cacheNames = "licenses"`

### Logging
- `log.info(...)` for business-logic-level events (create, update, delete)
- `log.debug(...)` for lookups and listing operations
- Concise messages with context parameters inline

### Testing
- Use `@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES or ALL_DEPENDENCIES, webEnvironment = RANDOM_PORT)`
- Use `@AutoConfigureWebTestClient` + `@Import(PostgresTestContainerConfiguration.class)`
- `WebTestClient` for HTTP integration testing
- `ApplicationEventPublisher` for event testing
- `JdbcTemplate` for raw DB assertions (check dimension state, cleanup)
- Request body construction: using `org.json.JSONObject`/`JSONArray` (not Jackson) in some tests
- Assertions: Spring `jsonPath` matchers or AssertJ `assertThat`
- Test fixture IDs use `System.nanoTime()` suffix to ensure uniqueness
