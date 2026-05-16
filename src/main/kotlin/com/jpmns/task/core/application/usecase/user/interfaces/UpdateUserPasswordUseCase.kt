package com.jpmns.task.core.application.usecase.user.interfaces

import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUserPasswordInputDTO

interface UpdateUserPasswordUseCase {
    fun execute(input: UpdateUserPasswordInputDTO)
}
