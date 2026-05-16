package com.jpmns.task.core.presentation.controller.payload.user.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

import com.jpmns.task.core.presentation.controller.documentation.payload.user.request.UpdateUsernameRequestDoc

data class UpdateUsernameRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 50)
    override val newUsername: String,
) : UpdateUsernameRequestDoc
