# Product Overview

Task Service is a RESTful API for task management built with Kotlin and Spring Boot, following Clean Architecture principles.

## Core Functionality

- User registration and authentication (JWT-based with access/refresh tokens)
- Full CRUD operations on tasks (create, list, update, delete, mark as finished)
- Each user can only access their own tasks (resource isolation)

## API Surface

- **Auth**: Login (`POST /api/v1/auth/login`), Refresh (`POST /api/v1/auth/refresh`)
- **Users**: Create, delete, update password, update username (`/api/v1/users`)
- **Tasks**: Create, list, update name, delete, finish (`/api/v1/tasks`)

## Key Constraints

- All task endpoints require Bearer token authentication
- Users can only manage their own tasks
- JWT secret must be at least 32 characters
- Swagger UI available at `/docs`
- Health/metrics at `/healthcheck`, `/info`, `/metrics`
