package com.jpmns.task.core.application.usecase.task.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.MarkTaskAsFinishedInputDTO
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException
import com.jpmns.task.core.application.usecase.task.interfaces.MarkTaskAsFinishedUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class MarkTaskAsFinishedUseCaseImpl(
    private val taskRepository: TaskRepository,
) : MarkTaskAsFinishedUseCase {
    override fun execute(input: MarkTaskAsFinishedInputDTO) {
        val taskIdResult = IdValueObject.of(input.taskId)
        if (taskIdResult.isFail) {
            throw taskIdResult.getRealError()
        }

        val taskId = taskIdResult.getRealValue()

        val task = taskRepository.findById(taskId)
            ?: throw TaskNotFoundException()

        if (task.userId.asString() != input.userId) {
            throw TaskAccessDeniedException()
        }

        task.markAsFinished()
        taskRepository.save(task)
    }
}
