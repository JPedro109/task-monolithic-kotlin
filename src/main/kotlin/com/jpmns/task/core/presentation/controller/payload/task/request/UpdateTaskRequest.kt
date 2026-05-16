package com.jpmns.task.core.presentation.controller.payload.task.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

import com.jpmns.task.core.presentation.controller.documentation.payload.task.request.UpdateTaskRequestDoc

data class UpdateTaskRequest(
    @field:NotBlank
    @field:Size(max = 255)
    override val taskName: String
) : UpdateTaskRequestDoc
