package com.jpmns.task.core.presentation.controller.payload.user.response

import com.jpmns.task.core.application.usecase.user.dto.output.CreateUserOutputDTO
import com.jpmns.task.core.presentation.controller.documentation.payload.user.response.CreateUserResponseDoc

data class CreateUserResponse(
    override val id: String,
    override val username: String,
) : CreateUserResponseDoc {
    companion object {
        fun of(dto: CreateUserOutputDTO): CreateUserResponse =
            CreateUserResponse(id = dto.id, username = dto.username)
    }
}
