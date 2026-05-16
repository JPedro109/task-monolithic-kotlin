package com.jpmns.task.core.application.usecase.task.interfaces

import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDTO
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO

interface CreateTaskUseCase {
    fun execute(input: CreateTaskInputDTO): TaskOutputDTO
}
