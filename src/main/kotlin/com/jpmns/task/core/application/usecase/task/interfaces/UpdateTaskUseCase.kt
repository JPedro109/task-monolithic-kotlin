package com.jpmns.task.core.application.usecase.task.interfaces

import com.jpmns.task.core.application.usecase.task.dto.input.UpdateTaskInputDTO
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO

interface UpdateTaskUseCase {
    fun execute(input: UpdateTaskInputDTO): TaskOutputDTO
}
