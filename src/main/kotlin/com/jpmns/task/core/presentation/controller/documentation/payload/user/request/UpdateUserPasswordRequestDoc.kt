package com.jpmns.task.core.presentation.controller.documentation.payload.user.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Payload para atualização da senha do usuário")
interface UpdateUserPasswordRequestDoc {

    @get:Schema(
        description = "Senha atual do usuário",
        example = "senha@123"
    )
    val currentPassword: String

    @get:Schema(
        description = "Nova senha (mínimo 8 caracteres)",
        example = "novaSenha@456",
        minLength = 8
    )
    val newPassword: String
}
