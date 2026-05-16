package com.jpmns.task.core.application.usecase.task.dto.output

import java.time.Instant

data class TaskOutputDTO(
    val id: String,
    val userId: String,
    val taskName: String,
    val finished: Boolean,
    val createdAt: Instant
)
