package com.jpmns.task.core.presentation.controller.documentation

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping

import com.jpmns.task.core.presentation.controller.payload.user.request.CreateUserRequest
import com.jpmns.task.core.presentation.controller.payload.user.request.UpdateUserPasswordRequest
import com.jpmns.task.core.presentation.controller.payload.user.request.UpdateUsernameRequest
import com.jpmns.task.core.presentation.controller.payload.user.response.CreateUserResponse
import com.jpmns.task.core.presentation.controller.payload.user.response.UpdateUsernameResponse

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Users", description = "Gerenciamento de usuários — criação, atualização e exclusão de conta")
@RequestMapping("/api/v1/users")
interface UserControllerDoc {

    @Operation(
        summary = "Criar novo usuário",
        description =
            "<p>Registra um novo usuário na plataforma.</p>" +
            "<p>Regras de validação:</p>" +
            "<ul>" +
            "<li>O username não pode já estar em uso por outro usuário</li>" +
            "</ul>" +
            "<p>Após o cadastro, utilize o endpoint " +
            "<code>POST /api/v1/auth/login</code> para obter os tokens de acesso.</p>",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = CreateUserRequest::class),
                examples = [
                    ExampleObject(
                        name = "Dados válidos",
                        value = """{"username": "joao_silva", "password": "senha@123"}"""
                    ),
                    ExampleObject(
                        name = "Username muito curto (inválido)",
                        value = """{"username": "jo", "password": "senha@123"}"""
                    ),
                    ExampleObject(
                        name = "Senha muito curta (inválido)",
                        value = """{"username": "joao_silva", "password": "123"}"""
                    )
                ]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Usuário criado com sucesso",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = CreateUserResponse::class),
                examples = [ExampleObject(
                    name = "Usuário criado",
                    value = """{"id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "username": "joao_silva"}"""
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
                        name = "Username muito curto",
                        value = """{"type": "about:blank", "title": "Bad Request", "status": 400, "detail": "username: size must be between 3 and 50"}"""
                    ),
                    ExampleObject(
                        name = "Senha muito curta",
                        value = """{"type": "about:blank", "title": "Bad Request", "status": 400, "detail": "password: size must be between 8 and 2147483647"}"""
                    )
                ]
            )]
        ),
        ApiResponse(
            responseCode = "409",
            description = "Username já está em uso",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Conflito de username",
                    value = """{"type": "about:blank", "title": "Conflict", "status": 409, "detail": "Username already exists"}"""
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
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<CreateUserResponse>

    @Operation(
        summary = "Excluir conta do usuário autenticado",
        description = "<p>Remove permanentemente a conta do usuário autenticado.</p>" +
            "<p><strong>Esta operação é irreversível.</strong> Todos os dados associados ao usuário, " +
            "incluindo suas tarefas, serão excluídos.</p>" +
            "<p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "204",
            description = "Conta excluída com sucesso — sem corpo de resposta"
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
            responseCode = "404",
            description = "Usuário não encontrado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Usuário não encontrado",
                    value = """{"type": "about:blank", "title": "Not Found", "status": 404, "detail": "User not found"}"""
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
    fun deleteUser(): ResponseEntity<Void>

    @Operation(
        summary = "Atualizar senha do usuário autenticado",
        description = "<p>Altera a senha da conta do usuário autenticado.</p>" +
            "<p>É necessário informar a <strong>senha atual</strong> para confirmar a identidade " +
            "antes de definir a nova senha.</p>" +
            "<p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UpdateUserPasswordRequest::class),
                examples = [
                    ExampleObject(
                        name = "Troca de senha válida",
                        value = """{"currentPassword": "senha@123", "newPassword": "novaSenha@456"}"""
                    ),
                    ExampleObject(
                        name = "Nova senha muito curta (inválido)",
                        value = """{"currentPassword": "senha@123", "newPassword": "123"}"""
                    )
                ]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "204",
            description = "Senha atualizada com sucesso — sem corpo de resposta"
        ),
        ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Nova senha muito curta",
                    value = """{"type": "about:blank", "title": "Bad Request", "status": 400, "detail": "newPassword: size must be between 8 and 2147483647"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Token inválido/expirado ou senha atual incorreta",
            content = [Content(
                mediaType = "application/json",
                examples = [
                    ExampleObject(
                        name = "Token inválido",
                        value = """{"type": "about:blank", "title": "Unauthorized", "status": 401, "detail": "Invalid or expired token"}"""
                    ),
                    ExampleObject(
                        name = "Senha atual incorreta",
                        value = """{"type": "about:blank", "title": "Unauthorized", "status": 401, "detail": "Invalid username or password"}"""
                    )
                ]
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
    fun updatePassword(@Valid @RequestBody request: UpdateUserPasswordRequest): ResponseEntity<Void>

    @Operation(
        summary = "Atualizar username do usuário autenticado",
        description = "<p>Altera o <strong>username</strong> da conta do usuário autenticado.</p>" +
            "<p>Regras de validação:</p>" +
            "<ul>" +
            "<li>O novo username não pode já estar em uso por outro usuário</li>" +
            "</ul>" +
            "<p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UpdateUsernameRequest::class),
                examples = [
                    ExampleObject(
                        name = "Novo username válido",
                        value = """{"newUsername": "joao_silva_novo"}"""
                    ),
                    ExampleObject(
                        name = "Username muito curto (inválido)",
                        value = """{"newUsername": "jo"}"""
                    )
                ]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Username atualizado com sucesso",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UpdateUsernameResponse::class),
                examples = [ExampleObject(
                    name = "Username atualizado",
                    value = """{"id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "username": "joao_silva_novo"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Username muito curto",
                    value = """{"type": "about:blank", "title": "Bad Request", "status": 400, "detail": "newUsername: size must be between 3 and 50"}"""
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
            responseCode = "409",
            description = "Novo username já está em uso",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Conflito de username",
                    value = """{"type": "about:blank", "title": "Conflict", "status": 409, "detail": "Username already exists"}"""
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
    fun updateUsername(@Valid @RequestBody request: UpdateUsernameRequest): ResponseEntity<UpdateUsernameResponse>
}
