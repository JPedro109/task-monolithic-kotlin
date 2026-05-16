package com.jpmns.task.core.presentation.controller.payload.task.response

import java.time.Instant

import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO
import com.jpmns.task.core.presentation.controller.documentation.payload.task.response.TaskResponseDoc

data class TaskResponse(
    override val id: String,
    override val userId: String,
    override val taskName: String,
    override val finished: Boolean,
    override val createdAt: Instant
) : TaskResponseDoc {
    companion object {
        fun of(dto: TaskOutputDTO): TaskResponse =
            TaskResponse(
                id = dto.id,
                userId = dto.userId,
                taskName = dto.taskName,
                finished = dto.finished,
                createdAt = dto.createdAt
            )
    }
}
