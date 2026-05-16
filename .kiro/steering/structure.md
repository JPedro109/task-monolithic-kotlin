# Project Structure

## Architecture: Clean Architecture (Ports & Adapters)

```
src/main/kotlin/com/jpmns/task/
├── TaskApplication.kt              # Spring Boot entry point
├── configuration/                  # Spring configuration beans
│   ├── security/                   # SecurityConfig (filter chain, CORS, CSRF)
│   ├── swagger/                    # SwaggerConfig (OpenAPI metadata)
│   └── tracing/                    # OtelBaggageConfig (observability)
├── core/
│   ├── domain/                     # Domain layer (entities, value objects, exceptions)
│   │   ├── common/
│   │   │   ├── abstracts/          # Base Entity class
│   │   │   ├── exception/          # Domain exceptions
│   │   │   └── valueobject/        # Shared value objects (IdValueObject)
│   │   ├── task/
│   │   │   ├── TaskEntity.kt
│   │   │   └── valueobject/        # TaskNameValueObject
│   │   └── user/
│   │       ├── UserEntity.kt
│   │       └── valueobject/        # UsernameValueObject, PasswordValueObject
│   ├── application/                # Application layer (use cases, ports)
│   │   ├── port/
│   │   │   ├── persistence/repository/   # Repository interfaces (TaskRepository, UserRepository)
│   │   │   └── security/                 # Security port interfaces (Token, PasswordEncoder)
│   │   └── usecase/
│   │       ├── task/
│   │       │   ├── interfaces/     # Use case interfaces (e.g., CreateTaskUseCase)
│   │       │   ├── implementation/ # Use case implementations (e.g., CreateTaskUseCaseImpl)
│   │       │   ├── dto/            # Input/Output DTOs
│   │       │   └── exception/      # Use case-specific exceptions
│   │       └── user/               # Same structure as task/
│   ├── external/                   # Infrastructure adapters
│   │   ├── persistence/
│   │   │   ├── dao/                # Spring Data JPA interfaces
│   │   │   ├── model/             # JPA entity models (DB representation)
│   │   │   ├── mapper/            # Domain ↔ JPA model mappers
│   │   │   └── repository/        # Repository adapter implementations
│   │   └── security/
│   │       ├── filter/             # JWT authentication filter
│   │       ├── service/            # UserDetailsService implementation
│   │       ├── TokenAdapter.kt     # JWT token adapter
│   │       └── PasswordEncoderAdapter.kt
│   └── presentation/              # Presentation layer
│       ├── controller/
│       │   ├── AuthController.kt
│       │   ├── TaskController.kt
│       │   ├── UserController.kt
│       │   ├── common/            # Shared controller concerns (exception handlers)
│       │   ├── documentation/     # Swagger annotations/interfaces
│       │   └── payload/           # Request/Response DTOs
│       └── scheduler/             # Scheduled tasks (if any)
└── shared/                        # Cross-cutting utilities
    ├── extension/                 # Kotlin extension functions
    └── type/                      # Shared types (Result<T, E>)
```

## Key Conventions

- **Use cases**: One interface per use case in `interfaces/`, one implementation in `implementation/` (suffix `Impl`)
- **Value objects**: Created via `of()` factory method returning `Result<T, E>`; validated at construction
- **Entities**: Extend base `Entity` class; validate all value objects in `init` block via `validateOrThrow()`
- **Ports**: Interfaces in `application/port/`; adapters in `external/`
- **DTOs**: Separate input/output DTOs per use case; never expose domain entities to controllers
- **Persistence**: JPA models in `external/persistence/model/` are distinct from domain entities; mappers handle conversion
- **Controllers**: Thin — delegate to use cases, handle HTTP concerns only

## Test Structure

```
src/test/kotlin/com/jpmns/task/     # Mirrors main source structure
src/test/resources/
├── application-integration-test.yaml  # Integration test config
└── sql/                               # SQL scripts for test data setup
```

## Infrastructure

```
docker/
├── docker-compose.yml              # PostgreSQL, Prometheus, Grafana, OTEL Collector
├── grafana/                        # Dashboard + provisioning configs
├── otel-collector/                 # OTEL Collector config
└── prometheus/                     # Prometheus scrape config
```
