package com.jpmns.task.core.application.usecase.task

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.UpdateTaskInputDto
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDto
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException
import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.task.TaskEntity

@Service
class UpdateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    @Transactional
    fun execute(input: UpdateTaskInputDto): TaskOutputDto {
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

    private fun toOutput(task: TaskEntity): TaskOutputDto =
        TaskOutputDto(
            id = task.id.asString(),
            userId = task.userId.asString(),
            taskName = task.taskName.asString(),
            finished = task.finished,
            createdAt = task.createdAt
        )
}
