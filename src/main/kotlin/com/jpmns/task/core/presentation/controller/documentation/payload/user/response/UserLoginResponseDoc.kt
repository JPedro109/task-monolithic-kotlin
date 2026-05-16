package com.jpmns.task.core.presentation.controller.documentation.payload.user.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Resposta de login com tokens de acesso")
interface UserLoginResponseDoc {

    @get:Schema(
        description = "Token de acesso (curta duração)",
        example = "eyJhbGciOiJSUzI1NiJ9..."
    )
    val accessToken: String

    @get:Schema(
        description = "Token de refresh (longa duração)",
        example = "eyJhbGciOiJSUzI1NiJ9..."
    )
    val refreshToken: String
}
