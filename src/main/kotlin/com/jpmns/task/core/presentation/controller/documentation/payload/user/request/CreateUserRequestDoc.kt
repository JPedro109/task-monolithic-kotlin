package com.jpmns.task.core.presentation.controller.documentation.payload.user.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Payload para criação de um novo usuário")
interface CreateUserRequestDoc {

    @get:Schema(
        description = "Nome de usuário único",
        example = "joao_silva",
        minLength = 3,
        maxLength = 50
    )
    val username: String

    @get:Schema(
        description = "Senha do usuário (mínimo 8 caracteres)",
        example = "senha@123",
        minLength = 8
    )
    val password: String
}
