# Visão Geral do Produto

O Task Service é uma API RESTful para gerenciamento de tarefas construída com Kotlin e Spring Boot, seguindo os princípios de Clean Architecture.

## Funcionalidades Principais

- Cadastro e autenticação de usuários (baseado em JWT com tokens de acesso e refresh)
- Operações CRUD completas em tarefas (criar, listar, atualizar, deletar, marcar como concluída)
- Cada usuário só pode acessar suas próprias tarefas (isolamento de recursos)

## Superfície da API

- **Auth**: Login (`POST /api/v1/auth/login`), Refresh (`POST /api/v1/auth/refresh`)
- **Usuários**: Criar, deletar, atualizar senha, atualizar username (`/api/v1/users`)
- **Tarefas**: Criar, listar, atualizar nome, deletar, concluir (`/api/v1/tasks`)

## Restrições Principais

- Todos os endpoints de tarefas requerem autenticação via Bearer token
- Usuários só podem gerenciar suas próprias tarefas
- O segredo JWT deve ter no mínimo 32 caracteres
- Swagger UI disponível em `/docs`
- Health/métricas em `/healthcheck`, `/info`, `/metrics`
