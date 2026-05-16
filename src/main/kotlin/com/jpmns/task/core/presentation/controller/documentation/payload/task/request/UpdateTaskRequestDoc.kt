package com.jpmns.task.core.presentation.controller.documentation.payload.task.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Payload para atualização do nome de uma tarefa")
interface UpdateTaskRequestDoc {

    @get:Schema(
        description = "Novo nome da tarefa",
        example = "Estudar Kotlin avançado",
        maxLength = 255
    )
    val taskName: String
}
