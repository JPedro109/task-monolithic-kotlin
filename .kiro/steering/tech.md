---
inclusion: always
---

# Stack Tecnológica

## Linguagem & Runtime

- Kotlin 2.0.21 com alvo JVM 21
- Flag do compilador: `-Xjsr305=strict` (interoperabilidade estrita de null-safety com Java)

## Framework

- Spring Boot 3.4.5
- Spring Security (autenticação JWT via JJWT 0.12.6)
- Spring Data JPA (Hibernate)
- Spring Validation (Jakarta Bean Validation)

## Banco de Dados

- PostgreSQL 15 (produção)
- Flyway para migrações de schema
- H2 para testes unitários, Testcontainers (PostgreSQL) para testes de integração

## Observabilidade

- Spring Boot Actuator
- Micrometer com registry OTLP
- OpenTelemetry (exportação de tracing + métricas)
- Prometheus + Grafana (via Docker Compose)

## Documentação

- SpringDoc OpenAPI (springdoc-openapi-starter-webmvc-ui 2.8.8)

## Sistema de Build

- Gradle com Kotlin DSL
- Plugins: `kotlin-jvm`, `kotlin-spring`, `kotlin-jpa`, `spring-boot`, `spring-dependency-management`

## Qualidade de Código

- **ktlint** 1.5.0 — formatação (política de zero warnings)
- **detekt** 1.23.8 — análise estática (configuração customizada em `detekt.yml`)
- **JaCoCo** — cobertura de código com threshold mínimo de 85%

## Testes

- JUnit 5 (JUnit Platform)
- MockK 1.13.13 + SpringMockK 4.0.2
- Testcontainers (PostgreSQL)
- Spring Security Test

## Comandos Comuns

```bash
# Rodar a aplicação
./gradlew bootRun

# Rodar os testes (requer Docker para o Testcontainers)
./gradlew test

# Verificação completa de qualidade (testes + cobertura + ktlint + detekt)
./gradlew check

# Formatar o código com ktlint
./gradlew ktlintFormat

# Subir a infraestrutura (PostgreSQL, Prometheus, Grafana, OTEL Collector)
cd docker && docker compose up -d
```

## Variáveis de Ambiente

A configuração de runtime fica em `src/main/resources/application.yaml` e é controlada por variáveis de ambiente, cada uma com um valor padrão adequado ao ambiente local.

| Variável | Padrão | Observações |
|----------|--------|-------------|
| `ENVIRONMENT_NAME` | `local` | Define o profile Spring ativo |
| `SERVER_PORT` | `8080` | Porta HTTP da aplicação |
| `APP_LOG_LEVEL` | `INFO` | Nível de log de `com.jpmns.task` |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `task` | Conexão com o PostgreSQL |
| `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` | Credenciais do banco |
| `JWT_SECRET` | `change-me-...` | Deve ter ≥ 32 caracteres; sempre substituir em produção |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` (15 min) | Expiração do access token |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7 dias) | Expiração do refresh token |
| `DOC_ENABLED` | `false` | Habilita o Swagger UI (`/docs`) e os api-docs |
| `ENABLE_TRACING` | `true` | Habilita a exportação de tracing |
| `ENABLE_OTLP_COLLECTOR` | `false` | Define como `true` para enviar métricas via OTLP |
| `OTLP_COLLECTOR_URL` | — | Obrigatório quando o OTLP estiver habilitado |
| `OTLP_COLLECTOR_PUSH_INTERVAL` | `1s` | Intervalo de envio das métricas OTLP |

## Observações

- O plugin `allOpen` abre entidades JPA, classes `@Service`, `@Component` e `@Repository`
- O plugin `noArg` gera construtores sem argumentos para entidades JPA
- A cobertura exclui: classes de configuração, DTOs e classes de exceção
