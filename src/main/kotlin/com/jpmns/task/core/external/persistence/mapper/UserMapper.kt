package com.jpmns.task.core.external.persistence.mapper

import java.util.UUID

import com.jpmns.task.core.domain.user.UserEntity
import com.jpmns.task.core.external.persistence.model.UserJpaModel

object UserMapper {
    fun toModel(entity: UserEntity): UserJpaModel =
        UserJpaModel(
            id = UUID.fromString(entity.id.asString()),
            username = entity.username.asString(),
            password = entity.password.asString(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toDomain(model: UserJpaModel): UserEntity =
        UserEntity(
            id = model.id.toString(),
            username = model.username,
            password = model.password,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt,
        )
}
