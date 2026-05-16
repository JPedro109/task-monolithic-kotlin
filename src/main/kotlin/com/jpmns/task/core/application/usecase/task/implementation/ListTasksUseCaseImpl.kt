package com.jpmns.task.core.application.usecase.task.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.ListTasksInputDTO
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO
import com.jpmns.task.core.application.usecase.task.interfaces.ListTasksUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class ListTasksUseCaseImpl(
    private val taskRepository: TaskRepository,
) : ListTasksUseCase {
    override fun execute(input: ListTasksInputDTO): List<TaskOutputDTO> {
        val userIdResult = IdValueObject.of(input.userId)
        if (userIdResult.isFail) {
            throw userIdResult.getFailureError()
        }

        val userId = userIdResult.getSuccessValue()

        return taskRepository.findAllByUserId(userId).map { task ->
            TaskOutputDTO(
                id = task.id.asString(),
                userId = task.userId.asString(),
                taskName = task.taskName.asString(),
                finished = task.finished,
                createdAt = task.createdAt
            )
        }
    }
}
