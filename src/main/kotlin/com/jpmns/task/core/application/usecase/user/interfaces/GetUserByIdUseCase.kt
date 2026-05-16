package com.jpmns.task.core.application.usecase.user.interfaces

import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.UserOutputDTO

interface GetUserByIdUseCase {
    fun execute(input: GetUserByIdInputDTO): UserOutputDTO
}
