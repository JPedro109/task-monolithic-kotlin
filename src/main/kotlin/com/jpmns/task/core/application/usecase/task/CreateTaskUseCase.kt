package com.jpmns.task.core.application.usecase.task

import java.util.UUID

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDto
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDto
import com.jpmns.task.core.domain.task.TaskEntity

@Service
class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    fun execute(input: CreateTaskInputDto): TaskOutputDto {
        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            userId = input.userId,
            taskName = input.taskName,
            finished = false
        )

        val saved = taskRepository.save(task)

        return toOutput(saved)
    }

    private fun toOutput(task: TaskEntity): TaskOutputDto =
        TaskOutputDto(
            id = task.id.asString(),
            userId = task.userId.asString(),
            taskName = task.taskName.asString(),
            finished = task.finished,
            createdAt = task.createdAt
        )
}
