package com.jpmns.task.core.presentation.controller.payload.user.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

import com.jpmns.task.core.presentation.controller.documentation.payload.user.request.UpdateUserPasswordRequestDoc

data class UpdateUserPasswordRequest(
    @field:NotBlank
    override val currentPassword: String,
    @field:NotBlank
    @field:Size(min = 8)
    override val newPassword: String
) : UpdateUserPasswordRequestDoc {
    override fun toString(): String =
        "UpdateUserPasswordRequest(" +
            "currentPassword=[REDACTED], newPassword=[REDACTED])"
}
