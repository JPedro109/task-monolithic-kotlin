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

## Observações

- O plugin `allOpen` abre entidades JPA, classes `@Service`, `@Component` e `@Repository`
- O plugin `noArg` gera construtores sem argumentos para entidades JPA
- A cobertura exclui: classes de configuração, DTOs e classes de exceção
