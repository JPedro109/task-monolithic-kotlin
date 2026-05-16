package com.jpmns.task.core.application.usecase.task.interfaces

import com.jpmns.task.core.application.usecase.task.dto.input.ListTasksInputDTO
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO

interface ListTasksUseCase {
    fun execute(input: ListTasksInputDTO): List<TaskOutputDTO>
}
