package com.jpmns.task.core.presentation.controller.documentation

import com.jpmns.task.core.presentation.controller.payload.user.request.RefreshTokenRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping

import com.jpmns.task.core.presentation.controller.payload.user.request.UserLoginRequest
import com.jpmns.task.core.presentation.controller.payload.user.response.RefreshTokenResponse
import com.jpmns.task.core.presentation.controller.payload.user.response.UserLoginResponse

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Auth", description = "Autenticação — login e renovação de tokens de acesso")
@RequestMapping("/api/v1/auth")
interface AuthControllerDoc {

    @Operation(
        summary = "Login do usuário",
        description = "<p>Autentica o usuário com username e senha.</p>" +
            "<p>Em caso de sucesso, retorna um <strong>accessToken</strong> (curta duração) " +
            "e um <strong>refreshToken</strong> (longa duração).</p>" +
            "<p>Utilize o <code>accessToken</code> no header <code>Authorization: Bearer " +
            "&lt;accessToken&gt;</code> para acessar os endpoints protegidos.</p>",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UserLoginRequest::class),
                examples = [
                    ExampleObject(
                        name = "Credenciais válidas",
                        value = """{"username": "joao_silva", "password": "senha@123"}"""
                    ),
                    ExampleObject(
                        name = "Senha incorreta (inválido)",
                        value = """{"username": "joao_silva", "password": "senhaErrada"}"""
                    )
                ]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Login realizado com sucesso",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UserLoginResponse::class),
                examples = [ExampleObject(
                    name = "Tokens gerados",
                    value = """{"accessToken": "eyJhbGciOiJSUzI1NiJ9...", "refreshToken": "eyJhbGciOiJSUzI1NiJ9..."}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Campo obrigatório ausente",
                    value = """{"type": "about:blank", "title": "Bad Request", "status": 400, "detail": "username: must not be blank"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Usuário ou senha incorretos",
                    value = """{"type": "about:blank", "title": "Unauthorized", "status": 401, "detail": "Invalid username or password"}"""
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
    fun login(@Valid @RequestBody request: UserLoginRequest): ResponseEntity<UserLoginResponse>

    @Operation(
        summary = "Renovar access token",
        description =
            "<p>Gera um novo <strong>accessToken</strong> a partir de um " +
            "<strong>refreshToken</strong> válido.</p>" +
            "<p>Utilize este endpoint quando o <code>accessToken</code> expirar, " +
            "evitando que o usuário precise fazer login novamente.</p>",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = RefreshTokenRequest::class),
                examples = [
                    ExampleObject(
                        name = "Refresh token válido",
                        value = """{"refreshToken": "eyJhbGciOiJSUzI1NiJ9..."}"""
                    ),
                    ExampleObject(
                        name = "Refresh token expirado (inválido)",
                        value = """{"refreshToken": "tokenExpiradoOuInvalido"}"""
                    )
                ]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Tokens renovados com sucesso",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = RefreshTokenResponse::class),
                examples = [ExampleObject(
                    name = "Novos tokens",
                    value = """{"accessToken": "eyJhbGciOiJSUzI1NiJ9...", "refreshToken": "eyJhbGciOiJSUzI1NiJ9..."}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Campo obrigatório ausente",
                    value = """{"type": "about:blank", "title": "Bad Request", "status": 400, "detail": "refreshToken: must not be blank"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Refresh token inválido ou expirado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(
                    name = "Token inválido",
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
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse>
}
