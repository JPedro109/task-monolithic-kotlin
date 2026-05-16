package com.jpmns.task.core.presentation.controller.payload.user.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

import com.jpmns.task.core.presentation.controller.documentation.payload.user.request.CreateUserRequestDoc

data class CreateUserRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 50)
    override val username: String,
    @field:NotBlank
    @field:Size(min = 8)
    override val password: String,
) : CreateUserRequestDoc {
    override fun toString(): String =
        "CreateUserRequest(username=$username, password=[REDACTED])"
}
