package com.jpmns.task.core.application.usecase.task.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.UpdateTaskInputDTO
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException
import com.jpmns.task.core.application.usecase.task.interfaces.UpdateTaskUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.task.TaskEntity

@Service
class UpdateTaskUseCaseImpl(
    private val taskRepository: TaskRepository
) : UpdateTaskUseCase {
    override fun execute(input: UpdateTaskInputDTO): TaskOutputDTO {
        val taskIdResult = IdValueObject.of(input.taskId).getValueResultOrThrow()

        val task = taskRepository.findById(taskIdResult) ?: throw TaskNotFoundException()

        val userIsOwner = task.userId.asString() == input.userId
        if (!userIsOwner) {
            throw TaskAccessDeniedException()
        }

        task.updateTaskName(input.taskName)

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
