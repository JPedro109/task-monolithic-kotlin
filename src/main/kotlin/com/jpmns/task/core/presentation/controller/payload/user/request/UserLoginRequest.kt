package com.jpmns.task.core.presentation.controller.payload.user.request

import jakarta.validation.constraints.NotBlank

import com.jpmns.task.core.presentation.controller.documentation.payload.user.request.UserLoginRequestDoc

data class UserLoginRequest(
    @field:NotBlank
    override val username: String,
    @field:NotBlank
    override val password: String,
) : UserLoginRequestDoc {
    override fun toString(): String =
        "UserLoginRequest(username=$username, password=[REDACTED])"
}
