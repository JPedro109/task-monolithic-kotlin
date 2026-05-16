package com.jpmns.task.core.presentation.controller.documentation.payload.task.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Payload para criação de uma nova tarefa")
interface CreateTaskRequestDoc {

    @get:Schema(
        description = "Nome da tarefa",
        example = "Estudar Kotlin",
        maxLength = 255
    )
    val taskName: String
}
