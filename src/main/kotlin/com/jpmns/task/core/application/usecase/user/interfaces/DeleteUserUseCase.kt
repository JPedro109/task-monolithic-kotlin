package com.jpmns.task.core.application.usecase.user.interfaces

import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDTO

interface DeleteUserUseCase {
    fun execute(input: DeleteUserInputDTO)
}
