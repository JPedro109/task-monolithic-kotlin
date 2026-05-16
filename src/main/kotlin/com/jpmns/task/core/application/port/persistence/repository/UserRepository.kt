package com.jpmns.task.core.application.port.persistence.repository

import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.user.UserEntity
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject

interface UserRepository {
    fun save(user: UserEntity): UserEntity

    fun findById(id: IdValueObject): UserEntity?

    fun findByUsername(username: UsernameValueObject): UserEntity?

    fun existsByUsername(username: UsernameValueObject): Boolean

    fun deleteById(id: IdValueObject)
}
