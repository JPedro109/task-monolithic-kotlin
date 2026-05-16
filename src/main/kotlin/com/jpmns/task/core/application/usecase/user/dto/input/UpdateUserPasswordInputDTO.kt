package com.jpmns.task.core.application.usecase.user.dto.input

data class UpdateUserPasswordInputDTO(
    val userId: String,
    val currentPassword: String,
    val newPassword: String
)
