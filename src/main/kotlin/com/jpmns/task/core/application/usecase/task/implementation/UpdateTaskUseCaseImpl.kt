package com.jpmns.task.core.application.usecase.task.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.UpdateTaskInputDTO
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException
import com.jpmns.task.core.application.usecase.task.interfaces.UpdateTaskUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class UpdateTaskUseCaseImpl(
    private val taskRepository: TaskRepository,
) : UpdateTaskUseCase {
    override fun execute(input: UpdateTaskInputDTO): TaskOutputDTO {
        val taskIdResult = IdValueObject.of(input.taskId)
        if (taskIdResult.isFail) {
            throw taskIdResult.getRealError()
        }

        val taskId = taskIdResult.getRealValue()

        val task = taskRepository.findById(taskId) ?: throw TaskNotFoundException()

        if (task.userId.asString() != input.userId) {
            throw TaskAccessDeniedException()
        }

        task.updateTaskName(input.taskName)
        val saved = taskRepository.save(task)

        return TaskOutputDTO(
            id = saved.id.asString(),
            userId = saved.userId.asString(),
            taskName = saved.taskName.asString(),
            finished = saved.finished,
            createdAt = saved.createdAt
        )
    }
}
