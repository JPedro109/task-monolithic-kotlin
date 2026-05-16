package com.jpmns.task.core.external.persistence.mapper

import java.util.UUID

import com.jpmns.task.core.domain.task.TaskEntity
import com.jpmns.task.core.external.persistence.model.TaskJpaModel

object TaskMapper {
    fun toModel(entity: TaskEntity): TaskJpaModel =
        TaskJpaModel(
            id = UUID.fromString(entity.id.asString()),
            userId = UUID.fromString(entity.userId.asString()),
            taskName = entity.taskName.asString(),
            finished = entity.finished,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )

    fun toDomain(model: TaskJpaModel): TaskEntity =
        TaskEntity(
            id = model.id.toString(),
            userId = model.userId.toString(),
            taskName = model.taskName,
            finished = model.finished,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
}
