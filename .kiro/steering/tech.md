# Tech Stack

## Language & Runtime

- Kotlin 2.0.21 targeting JVM 21
- Compiler flag: `-Xjsr305=strict` (strict null-safety interop with Java)

## Framework

- Spring Boot 3.4.5
- Spring Security (JWT authentication via JJWT 0.12.6)
- Spring Data JPA (Hibernate)
- Spring Validation (Jakarta Bean Validation)

## Database

- PostgreSQL 15 (production)
- Flyway for schema migrations
- H2 for unit tests, Testcontainers (PostgreSQL) for integration tests

## Observability

- Spring Boot Actuator
- Micrometer with OTLP registry
- OpenTelemetry (tracing + metrics export)
- Prometheus + Grafana (via Docker Compose)

## Documentation

- SpringDoc OpenAPI (springdoc-openapi-starter-webmvc-ui 2.8.8)

## Build System

- Gradle with Kotlin DSL
- Plugins: `kotlin-jvm`, `kotlin-spring`, `kotlin-jpa`, `spring-boot`, `spring-dependency-management`

## Code Quality

- **ktlint** 1.5.0 — formatting (zero warnings policy)
- **detekt** 1.23.8 — static analysis (custom config in `detekt.yml`)
- **JaCoCo** — code coverage with 85% minimum threshold

## Testing

- JUnit 5 (JUnit Platform)
- MockK 1.13.13 + SpringMockK 4.0.2
- Testcontainers (PostgreSQL)
- Spring Security Test

## Common Commands

```bash
# Run the application
./gradlew bootRun

# Run tests (requires Docker for Testcontainers)
./gradlew test

# Full quality check (tests + coverage + ktlint + detekt)
./gradlew check

# Format code with ktlint
./gradlew ktlintFormat

# Start infrastructure (PostgreSQL, Prometheus, Grafana, OTEL Collector)
cd docker && docker compose up -d
```

## Notes

- `allOpen` plugin opens JPA entities, `@Service`, `@Component`, `@Repository` classes
- `noArg` plugin generates no-arg constructors for JPA entities
- Coverage excludes: configuration classes, DTOs, and exception classes
