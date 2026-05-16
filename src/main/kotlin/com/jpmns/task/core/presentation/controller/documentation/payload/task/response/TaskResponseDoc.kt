package com.jpmns.task.core.presentation.controller.documentation.payload.task.response

import java.time.Instant

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Representação de uma tarefa")
interface TaskResponseDoc {

    @get:Schema(
        description = "Identificador único da tarefa",
        example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    )
    val id: String

    @get:Schema(
        description = "Identificador do usuário proprietário",
        example = "b2c3d4e5-f6a7-8901-bcde-f12345678901"
    )
    val userId: String

    @get:Schema(
        description = "Nome da tarefa",
        example = "Estudar Kotlin"
    )
    val taskName: String

    @get:Schema(
        description = "Indica se a tarefa foi concluída",
        example = "false"
    )
    val finished: Boolean

    @get:Schema(
        description = "Data e hora de criação da tarefa",
        example = "2024-01-15T10:30:00Z"
    )
    val createdAt: Instant
}
