package com.jpmns.task.core.presentation.controller.documentation.payload.user.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Payload para autenticação do usuário")
interface UserLoginRequestDoc {

    @get:Schema(
        description = "Nome de usuário",
        example = "joao_silva"
    )
    val username: String

    @get:Schema(
        description = "Senha do usuário",
        example = "senha@123"
    )
    val password: String
}
