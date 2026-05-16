# Task Service

API RESTful para gerenciamento de tarefas construída com Kotlin 2.0 e Spring Boot 3.4, seguindo os princípios da Clean Architecture. Usuários podem se cadastrar, autenticar e gerenciar suas próprias tarefas com suporte completo a CRUD.

## Índice

- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Como Executar](#como-executar)
- [Pré-requisitos](#pré-requisitos)
- [Executando com Docker](#executando-com-docker)
- [Executando Localmente](#executando-localmente)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Referência da API](#referência-da-api)
- [Autenticação](#autenticação)
- [Usuários](#usuários)
- [Tarefas](#tarefas)
- [Observabilidade](#observabilidade)
- [Testes](#testes)
- [Qualidade de Código](#qualidade-de-código)

---

## Arquitetura

O projeto segue a **Clean Architecture**, organizado em quatro camadas principais:

```
src/main/kotlin/com/jpmns/task/
├── configuration/        # Configurações do Spring (Security, Swagger, Tracing)
├── core/
│   ├── domain/           # Entidades e value objects (regras de negócio)
│   ├── application/      # Casos de uso e interfaces de porta
│   ├── external/         # Adaptadores: repositórios JPA, JWT, encoder de senha
│   └── presentation/     # Controllers REST e payloads de request/response
└── shared/               # Tipos e utilitários compartilhados
```

Principais decisões de design:

- **Casos de uso** são definidos como interfaces e implementados separadamente, mantendo o domínio desacoplado de frameworks.
- **Value objects** garantem invariantes no nível do domínio (ex.: `TaskNameValueObject`, `UsernameValueObject`).
- **Interfaces de porta** (`TaskRepository`, `Token`, `PasswordEncoder`) isolam o domínio de preocupações de infraestrutura.

---

## Tecnologias

| Categoria        | Tecnologia                                      |
|------------------|-------------------------------------------------|
| Linguagem        | Kotlin 2.0.21 (JVM 21)                         |
| Framework        | Spring Boot 3.4.5                               |
| Banco de Dados   | PostgreSQL 15                                   |
| Migrações        | Flyway                                          |
| Segurança        | Spring Security + JWT (JJWT 0.12.6)            |
| Documentação     | SpringDoc OpenAPI (Swagger UI)                  |
| Observabilidade  | OpenTelemetry, Micrometer, Prometheus, Grafana  |
| Testes           | JUnit 5, MockK, Testcontainers, JaCoCo         |
| Qualidade        | ktlint, detekt                                  |
| Build            | Gradle (Kotlin DSL)                             |

---

## Como Executar

### Pré-requisitos

- [Java 21+](https://adoptium.net/)
- [Docker](https://www.docker.com/) e Docker Compose

### Executando com Docker

Inicie todos os serviços de infraestrutura (PostgreSQL, Prometheus, Grafana, OpenTelemetry Collector):

```bash
cd docker
docker compose up -d
```

Em seguida, execute a aplicação:

```bash
./gradlew bootRun
```

A API estará disponível em `http://localhost:8080`.

### Executando Localmente

1. Certifique-se de que o PostgreSQL está em execução e acessível.
2. Configure as variáveis de ambiente necessárias (veja [Variáveis de Ambiente](#variáveis-de-ambiente)).
3. Execute a aplicação:

```bash
./gradlew bootRun
```

---

## Variáveis de Ambiente

| Variável                       | Descrição                                        | Padrão                                               |
|--------------------------------|--------------------------------------------------|------------------------------------------------------|
| `DB_HOST`                      | Host do PostgreSQL                               | `localhost`                                          |
| `DB_PORT`                      | Porta do PostgreSQL                              | `5432`                                               |
| `DB_NAME`                      | Nome do banco de dados                           | `task`                                               |
| `DB_USER`                      | Usuário do banco de dados                        | `postgres`                                           |
| `DB_PASSWORD`                  | Senha do banco de dados                          | `postgres`                                           |
| `JWT_SECRET`                   | Chave secreta para assinatura JWT (mín. 32 chars)| `change-me-in-production-must-be-at-least-32-chars`  |
| `JWT_ACCESS_EXPIRATION_MS`     | Expiração do access token em milissegundos       | `900000` (15 min)                                    |
| `JWT_REFRESH_EXPIRATION_MS`    | Expiração do refresh token em milissegundos      | `604800000` (7 dias)                                 |
| `ENABLE_OTLP_COLLECTOR`        | Habilita exportação de métricas via OTLP         | `false`                                              |
| `OTLP_COLLECTOR_URL`           | URL do endpoint do coletor OTLP                  | —                                                    |
| `OTLP_COLLECTOR_PUSH_INTERVAL` | Intervalo de envio de métricas                   | `1s`                                                 |

> **Importante:** Sempre substitua o `JWT_SECRET` em produção por um valor forte gerado aleatoriamente.

---

## Referência da API

A documentação interativa está disponível via Swagger UI em:

```
http://localhost:8080/docs
```

### Autenticação

| Método | Endpoint               | Autenticação | Descrição                          |
|--------|------------------------|--------------|-------------------------------------|
| POST   | `/api/v1/auth/login`   | Não          | Autentica e retorna os tokens      |
| POST   | `/api/v1/auth/refresh` | Não          | Renova o access token              |

**Requisição de login:**

```json
{
  "username": "joao",
  "password": "senha123"
}
```

**Resposta de login:**

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>"
}
```

### Usuários

| Método | Endpoint                    | Autenticação | Descrição                        |
|--------|-----------------------------|--------------|----------------------------------|
| POST   | `/api/v1/users`             | Não          | Cria um novo usuário             |
| DELETE | `/api/v1/users`             | Sim          | Remove o usuário autenticado     |
| PATCH  | `/api/v1/users/password`    | Sim          | Atualiza a senha                 |
| PATCH  | `/api/v1/users/username`    | Sim          | Atualiza o nome de usuário       |

### Tarefas

Todos os endpoints de tarefas exigem o header `Authorization: Bearer <accessToken>`. Cada usuário só pode acessar suas próprias tarefas.

| Método | Endpoint                        | Descrição                          |
|--------|---------------------------------|------------------------------------|
| POST   | `/api/v1/tasks`                 | Cria uma nova tarefa               |
| GET    | `/api/v1/tasks`                 | Lista todas as tarefas do usuário  |
| PUT    | `/api/v1/tasks/{taskId}`        | Atualiza o nome de uma tarefa      |
| DELETE | `/api/v1/tasks/{taskId}`        | Remove uma tarefa                  |
| PATCH  | `/api/v1/tasks/{taskId}/finish` | Marca uma tarefa como concluída    |

---

## Observabilidade

O serviço expõe métricas e rastreamentos via OpenTelemetry. Ao executar o stack completo com Docker Compose:

| Serviço        | URL                                          | Credenciais   |
|----------------|----------------------------------------------|---------------|
| Prometheus     | http://localhost:9090                        | —             |
| Grafana        | http://localhost:3000                        | admin / admin |
| OTEL Collector | http://localhost:4317 (gRPC) / :4318 (HTTP)  | —             |

Um dashboard do Grafana para o serviço de tarefas é provisionado automaticamente.

Os endpoints do Spring Boot Actuator estão disponíveis em `/healthcheck` (health), `/info` e `/metrics`.

---

## Testes

```bash
./gradlew test
```

Os testes utilizam **Testcontainers** para subir uma instância real do PostgreSQL, portanto o Docker precisa estar em execução.

Após a execução dos testes, um relatório HTML de cobertura do JaCoCo é gerado em:

```
build/reports/jacoco/test/html/index.html
```

O build exige uma cobertura mínima de **85%**. A build falhará caso esse limite não seja atingido.

---

## Qualidade de Código

O projeto utiliza **ktlint** para formatação e **detekt** para análise estática, aplicados em todo build:

```bash
./gradlew check
```

Esse comando executa os testes, a verificação de cobertura, o ktlint e o detekt em conjunto. Nenhum warning é permitido.
