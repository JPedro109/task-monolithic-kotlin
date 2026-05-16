package com.jpmns.task.core.presentation.controller.documentation.payload.user.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Resposta de renovação de tokens")
interface RefreshTokenResponseDoc {

    @get:Schema(
        description = "Novo token de acesso (curta duração)",
        example = "eyJhbGciOiJSUzI1NiJ9..."
    )
    val accessToken: String

    @get:Schema(
        description = "Novo token de refresh (longa duração)",
        example = "eyJhbGciOiJSUzI1NiJ9..."
    )
    val refreshToken: String
}
