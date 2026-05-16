package com.jpmns.task.core.presentation.controller.documentation.payload.user.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Resposta de criação de usuário")
interface CreateUserResponseDoc {

    @get:Schema(
        description = "Identificador único do usuário",
        example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    )
    val id: String

    @get:Schema(
        description = "Nome de usuário",
        example = "joao_silva"
    )
    val username: String
}
