package com.jpmns.task.core.application.usecase.task.implementation

import java.util.UUID

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDTO
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO
import com.jpmns.task.core.application.usecase.task.interfaces.CreateTaskUseCase
import com.jpmns.task.core.domain.task.TaskEntity

@Service
class CreateTaskUseCaseImpl(
    private val taskRepository: TaskRepository
) : CreateTaskUseCase {
    override fun execute(input: CreateTaskInputDTO): TaskOutputDTO {
        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            userId = input.userId,
            taskName = input.taskName,
            finished = false
        )

        val saved = taskRepository.save(task)

        return toOutput(saved)
    }

    private fun toOutput(task: TaskEntity): TaskOutputDTO =
        TaskOutputDTO(
            id = task.id.asString(),
            userId = task.userId.asString(),
            taskName = task.taskName.asString(),
            finished = task.finished,
            createdAt = task.createdAt
        )
}
