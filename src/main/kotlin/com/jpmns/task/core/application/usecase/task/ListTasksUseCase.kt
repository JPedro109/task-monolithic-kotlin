package com.jpmns.task.core.application.usecase.task

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.ListTasksInputDto
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDto
import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.task.TaskEntity

@Service
class ListTasksUseCase(
    private val taskRepository: TaskRepository
) {
    fun execute(input: ListTasksInputDto): List<TaskOutputDto> {
        val userIdResult = IdValueObject.of(input.userId).getValueResultOrThrow()

        return taskRepository.findAllByUserId(userIdResult).map(::toOutput)
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
