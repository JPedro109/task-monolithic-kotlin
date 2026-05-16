package com.jpmns.task.core.presentation.controller.documentation

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping

import com.jpmns.task.core.presentation.controller.payload.task.request.CreateTaskRequest
import com.jpmns.task.core.presentation.controller.payload.task.request.UpdateTaskRequest
import com.jpmns.task.core.presentation.controller.payload.task.response.TaskResponse

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Tasks", description = "Gerenciamento de tarefas — criação, listagem, atualização e exclusão")
@RequestMapping("/api/v1/tasks")
@SecurityRequirement(name = "bearerAuth")
interface TaskControllerDoc {

    @Operation(
        summary = "Criar nova tarefa",
        description = "<p>Cria uma nova tarefa vinculada ao usuário autenticado.</p>" +
            "<p>Regras de validação:</p>" +
            "<ul>" +
            "<li>O nome da tarefa não pode ser vazio</li>" +
            "<li>O nome da tarefa não pode ultrapassar 255 caracteres</li>" +
            "</ul>" +
            "<p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = CreateTaskRequest::class),
                examples = [
                    ExampleObject(
                        name = "Dados válidos",
                        value = """{"taskName": "Estudar Kotlin"}"""
                    ),
                    ExampleObject(
                        name = "Nome vazio (inválido)",
                        value = """{"taskName": ""}"""
                    )
                ]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Tarefa criada com sucesso",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = TaskResponse::class),
                examples = [ExampleObject(
                    name = "Tarefa criada",
                    value = """{"id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901", "taskName": "Estudar Kotlin", "finished": false, "createdAt": "2024-01-15T10:30:00Z"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = [Content(
                mediaType = "application/json",
                examples = [
                    ExampleObject(
                        name = "Nome vazio",
                        value = """{"type": "about:blank", "title": "Bad Request", "status": 400, "detail": "taskName: must not be blank"}"""
                    ),
                    ExampleObject(
                        name = "Nome muito longo",
                        value = """{"type": "about:blank", "title": "Bad Request", "status": 400, "detail": "taskName: size must be between 0 and 255"}"""
                    )
                ]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Não autenticado",
                    value = """{"type": "about:blank", "title": "Unauthorized", "status": 401, "detail": "Invalid or expired token"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Erro interno",
                    value = """{"type": "about:blank", "title": "Internal Server Error", "status": 500, "detail": "Internal server error"}"""
                )]
            )]
        )
    )
    fun createTask(@Valid @org.springframework.web.bind.annotation.RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse>

    @Operation(
        summary = "Listar tarefas do usuário autenticado",
        description = "<p>Retorna todas as tarefas pertencentes ao usuário autenticado.</p>" +
            "<p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Lista de tarefas retornada com sucesso",
            content = [Content(
                mediaType = "application/json",
                examples = [
                    ExampleObject(
                        name = "Com tarefas",
                        value = """[{"id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901", "taskName": "Estudar Kotlin", "finished": false, "createdAt": "2024-01-15T10:30:00Z"}, {"id": "c3d4e5f6-a7b8-9012-cdef-123456789012", "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901", "taskName": "Revisar PR", "finished": true, "createdAt": "2024-01-14T08:00:00Z"}]"""
                    ),
                    ExampleObject(
                        name = "Sem tarefas",
                        value = """[]"""
                    )
                ]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Não autenticado",
                    value = """{"type": "about:blank", "title": "Unauthorized", "status": 401, "detail": "Invalid or expired token"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Erro interno",
                    value = """{"type": "about:blank", "title": "Internal Server Error", "status": 500, "detail": "Internal server error"}"""
                )]
            )]
        )
    )
    fun listTasks(): ResponseEntity<List<TaskResponse>>

    @Operation(
        summary = "Atualizar tarefa",
        description = "<p>Atualiza o nome de uma tarefa existente.</p>" +
            "<p>O usuário autenticado só pode atualizar suas próprias tarefas.</p>" +
            "<p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UpdateTaskRequest::class),
                examples = [
                    ExampleObject(
                        name = "Dados válidos",
                        value = """{"taskName": "Estudar Kotlin avançado"}"""
                    ),
                    ExampleObject(
                        name = "Nome vazio (inválido)",
                        value = """{"taskName": ""}"""
                    )
                ]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Tarefa atualizada com sucesso",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = TaskResponse::class),
                examples = [ExampleObject(
                    name = "Tarefa atualizada",
                    value = """{"id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901", "taskName": "Estudar Kotlin avançado", "finished": false, "createdAt": "2024-01-15T10:30:00Z"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Nome vazio",
                    value = """{"type": "about:blank", "title": "Bad Request", "status": 400, "detail": "taskName: must not be blank"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Não autenticado",
                    value = """{"type": "about:blank", "title": "Unauthorized", "status": 401, "detail": "Invalid or expired token"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "403",
            description = "Acesso negado — tarefa pertence a outro usuário",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Acesso negado",
                    value = """{"type": "about:blank", "title": "Forbidden", "status": 403, "detail": "Access denied"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Tarefa não encontrada",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Tarefa não encontrada",
                    value = """{"type": "about:blank", "title": "Not Found", "status": 404, "detail": "Task not found"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Erro interno",
                    value = """{"type": "about:blank", "title": "Internal Server Error", "status": 500, "detail": "Internal server error"}"""
                )]
            )]
        )
    )
    fun updateTask(taskId: String, @Valid @org.springframework.web.bind.annotation.RequestBody request: UpdateTaskRequest): ResponseEntity<TaskResponse>

    @Operation(
        summary = "Excluir tarefa",
        description = "<p>Remove permanentemente uma tarefa.</p>" +
            "<p>O usuário autenticado só pode excluir suas próprias tarefas.</p>" +
            "<p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "204",
            description = "Tarefa excluída com sucesso — sem corpo de resposta"
        ),
        ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Não autenticado",
                    value = """{"type": "about:blank", "title": "Unauthorized", "status": 401, "detail": "Invalid or expired token"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "403",
            description = "Acesso negado — tarefa pertence a outro usuário",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Acesso negado",
                    value = """{"type": "about:blank", "title": "Forbidden", "status": 403, "detail": "Access denied"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Tarefa não encontrada",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Tarefa não encontrada",
                    value = """{"type": "about:blank", "title": "Not Found", "status": 404, "detail": "Task not found"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Erro interno",
                    value = """{"type": "about:blank", "title": "Internal Server Error", "status": 500, "detail": "Internal server error"}"""
                )]
            )]
        )
    )
    fun deleteTask(taskId: String): ResponseEntity<Void>

    @Operation(
        summary = "Marcar tarefa como concluída",
        description = "<p>Marca uma tarefa como <strong>concluída</strong> (finished = true).</p>" +
            "<p>O usuário autenticado só pode alterar suas próprias tarefas.</p>" +
            "<p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "204",
            description = "Tarefa marcada como concluída — sem corpo de resposta"
        ),
        ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Não autenticado",
                    value = """{"type": "about:blank", "title": "Unauthorized", "status": 401, "detail": "Invalid or expired token"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "403",
            description = "Acesso negado — tarefa pertence a outro usuário",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Acesso negado",
                    value = """{"type": "about:blank", "title": "Forbidden", "status": 403, "detail": "Access denied"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Tarefa não encontrada",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Tarefa não encontrada",
                    value = """{"type": "about:blank", "title": "Not Found", "status": 404, "detail": "Task not found"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Erro interno",
                    value = """{"type": "about:blank", "title": "Internal Server Error", "status": 500, "detail": "Internal server error"}"""
                )]
            )]
        )
    )
    fun markTaskAsFinished(taskId: String): ResponseEntity<Void>
}
