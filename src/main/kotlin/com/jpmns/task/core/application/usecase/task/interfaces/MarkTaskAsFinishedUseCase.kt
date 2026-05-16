package com.jpmns.task.core.application.usecase.task.interfaces

import com.jpmns.task.core.application.usecase.task.dto.input.MarkTaskAsFinishedInputDTO

interface MarkTaskAsFinishedUseCase {
    fun execute(input: MarkTaskAsFinishedInputDTO)
}
