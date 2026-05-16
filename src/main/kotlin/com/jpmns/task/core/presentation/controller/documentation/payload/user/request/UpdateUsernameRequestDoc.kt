package com.jpmns.task.core.presentation.controller.documentation.payload.user.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Payload para atualização do username")
interface UpdateUsernameRequestDoc {

    @get:Schema(
        description = "Novo nome de usuário",
        example = "joao_silva_novo",
        minLength = 3,
        maxLength = 50
    )
    val newUsername: String
}
