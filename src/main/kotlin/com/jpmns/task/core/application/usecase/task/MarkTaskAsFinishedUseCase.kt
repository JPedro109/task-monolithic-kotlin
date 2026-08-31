package com.jpmns.task.core.application.usecase.task

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.MarkTaskAsFinishedInputDTO
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class MarkTaskAsFinishedUseCase(
    private val taskRepository: TaskRepository
) {
    fun execute(input: MarkTaskAsFinishedInputDTO) {
        val taskIdResult = IdValueObject.of(input.taskId).getValueResultOrThrow()

        val task = taskRepository.findById(taskIdResult)
            ?: throw TaskNotFoundException()

        val userIsOwner = task.userId.asString() == input.userId
        if (!userIsOwner) {
            throw TaskAccessDeniedException()
        }

        task.markAsFinished()

        taskRepository.save(task)
    }
}
