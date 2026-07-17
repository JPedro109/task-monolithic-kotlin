package com.jpmns.task.core.application.usecase.user.dto.output

import java.time.Instant

data class UserOutputDTO(
    val id: String,
    val username: String,
    val createdAt: Instant,
    val updatedAt: Instant?
)
