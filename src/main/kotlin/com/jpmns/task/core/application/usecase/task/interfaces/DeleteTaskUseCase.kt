package com.jpmns.task.core.application.usecase.task.interfaces

import com.jpmns.task.core.application.usecase.task.dto.input.DeleteTaskInputDTO

interface DeleteTaskUseCase {
    fun execute(input: DeleteTaskInputDTO)
}
