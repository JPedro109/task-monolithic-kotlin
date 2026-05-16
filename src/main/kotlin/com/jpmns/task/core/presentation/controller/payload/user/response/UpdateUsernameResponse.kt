package com.jpmns.task.core.presentation.controller.payload.user.response

import com.jpmns.task.core.application.usecase.user.dto.output.UpdateUsernameOutputDTO
import com.jpmns.task.core.presentation.controller.documentation.payload.user.response.UpdateUsernameResponseDoc

data class UpdateUsernameResponse(
    override val id: String,
    override val username: String,
) : UpdateUsernameResponseDoc {
    companion object {
        fun of(dto: UpdateUsernameOutputDTO): UpdateUsernameResponse =
            UpdateUsernameResponse(
                id = dto.id,
                username = dto.username
            )
    }
}
