package com.jpmns.task.core.presentation.controller.documentation.payload.user.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Payload para renovação do token de acesso")
interface RefreshTokenRequestDoc {

    @get:Schema(
        description = "Token de refresh válido",
        example = "eyJhbGciOiJSUzI1NiJ9..."
    )
    val refreshToken: String
}
