package com.jpmns.task.core.application.usecase.user.dto.input

data class UpdateUserPasswordInputDto(
    val userId: String,
    val currentPassword: String,
    val newPassword: String
)
