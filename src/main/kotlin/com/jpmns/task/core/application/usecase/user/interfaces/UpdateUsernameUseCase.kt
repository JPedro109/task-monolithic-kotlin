package com.jpmns.task.core.application.usecase.user.interfaces

import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUsernameInputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.UpdateUsernameOutputDTO

interface UpdateUsernameUseCase {
    fun execute(input: UpdateUsernameInputDTO): UpdateUsernameOutputDTO
}
