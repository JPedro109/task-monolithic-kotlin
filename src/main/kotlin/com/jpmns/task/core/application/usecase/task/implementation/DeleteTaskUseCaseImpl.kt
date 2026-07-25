package com.jpmns.task.core.application.usecase.task.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.DeleteTaskInputDTO
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException
import com.jpmns.task.core.application.usecase.task.interfaces.DeleteTaskUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class DeleteTaskUseCaseImpl(
    private val taskRepository: TaskRepository
) : DeleteTaskUseCase {
    override fun execute(input: DeleteTaskInputDTO) {
        val taskIdResult = IdValueObject.of(input.taskId).getValueResultOrThrow()

        val task = taskRepository.findById(taskIdResult)
            ?: throw TaskNotFoundException()

        val userIsOwner = task.userId.asString() == input.userId
        if (!userIsOwner) {
            throw TaskAccessDeniedException()
        }

        taskRepository.deleteById(taskIdResult)
    }
}
