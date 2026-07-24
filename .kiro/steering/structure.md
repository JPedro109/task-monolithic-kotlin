# Estrutura do Projeto

## Arquitetura: Clean Architecture (Ports & Adapters)

```
src/main/kotlin/com/jpmns/task/
├── TaskApplication.kt              # Ponto de entrada do Spring Boot
├── configuration/                  # Beans de configuração do Spring
│   ├── security/                   # SecurityConfig (filter chain, CORS, CSRF)
│   ├── swagger/                    # SwaggerConfig (metadados OpenAPI)
│   └── tracing/                    # OtelBaggageConfig (observabilidade)
├── core/
│   ├── domain/                     # Camada de domínio (entidades, value objects, exceções)
│   │   ├── common/
│   │   │   ├── abstracts/          # Classe base Entity
│   │   │   ├── exception/          # Exceções de domínio
│   │   │   └── valueobject/        # Value objects compartilhados (IdValueObject)
│   │   ├── task/
│   │   │   ├── TaskEntity.kt
│   │   │   └── valueobject/        # TaskNameValueObject
│   │   └── user/
│   │       ├── UserEntity.kt
│   │       └── valueobject/        # UsernameValueObject, PasswordValueObject
│   ├── application/                # Camada de aplicação (casos de uso, ports)
│   │   ├── port/
│   │   │   ├── persistence/repository/   # Interfaces de repositório (TaskRepository, UserRepository)
│   │   │   └── security/                 # Interfaces de port de segurança (Token, PasswordEncoder)
│   │   └── usecase/
│   │       ├── task/
│   │       │   ├── interfaces/     # Interfaces de casos de uso (ex: CreateTaskUseCase)
│   │       │   ├── implementation/ # Implementações dos casos de uso (ex: CreateTaskUseCaseImpl)
│   │       │   ├── dto/            # DTOs de entrada/saída
│   │       │   └── exception/      # Exceções específicas do caso de uso
│   │       └── user/               # Mesma estrutura de task/
│   ├── external/                   # Adaptadores de infraestrutura
│   │   ├── persistence/
│   │   │   ├── dao/                # Interfaces Spring Data JPA
│   │   │   ├── model/             # Modelos de entidade JPA (representação no banco)
│   │   │   ├── mapper/            # Mappers domínio ↔ modelo JPA
│   │   │   └── repository/        # Implementações dos adaptadores de repositório
│   │   └── security/
│   │       ├── filter/             # Filtro de autenticação JWT
│   │       ├── service/            # Implementação do UserDetailsService
│   │       ├── TokenAdapter.kt     # Adaptador de token JWT
│   │       └── PasswordEncoderAdapter.kt
│   └── presentation/              # Camada de apresentação
│       ├── controller/
│       │   ├── AuthController.kt
│       │   ├── TaskController.kt
│       │   ├── UserController.kt
│       │   ├── common/            # Preocupações comuns dos controllers (handlers de exceção)
│       │   ├── documentation/     # Anotações/interfaces Swagger
│       │   └── payload/           # DTOs de requisição/resposta
│       └── scheduler/             # Tarefas agendadas (se houver)
└── shared/                        # Utilitários transversais
    ├── extension/                 # Funções de extensão Kotlin
    └── type/                      # Tipos compartilhados (Result<T, E>)
```

## Convenções Principais

- **Casos de uso**: Uma interface por caso de uso em `interfaces/`, uma implementação em `implementation/` (sufixo `Impl`)
- **Value objects**: Criados via método factory `of()` retornando `Result<T, E>`; validados na construção
- **Entidades**: Estendem a classe base `Entity`; validam todos os value objects no bloco `init` via `validateOrThrow()`
- **Ports**: Interfaces em `application/port/`; adaptadores em `external/`
- **DTOs**: DTOs de entrada/saída separados por caso de uso; nunca expor entidades de domínio para os controllers
- **Persistência**: Modelos JPA em `external/persistence/model/` são distintos das entidades de domínio; mappers fazem a conversão
- **Controllers**: Enxutos — delegam para casos de uso, tratam apenas preocupações HTTP

## Estrutura de Testes

```
src/test/kotlin/com/jpmns/task/     # Espelha a estrutura do código principal
src/test/resources/
├── application-integration-test.yaml  # Configuração dos testes de integração
└── sql/                               # Scripts SQL para configuração de dados de teste
```

## Infraestrutura

```
docker/
├── docker-compose.yml              # PostgreSQL, Prometheus, Grafana, OTEL Collector
├── grafana/                        # Configurações de dashboard e provisioning
├── otel-collector/                 # Configuração do OTEL Collector
└── prometheus/                     # Configuração de scrape do Prometheus
```
