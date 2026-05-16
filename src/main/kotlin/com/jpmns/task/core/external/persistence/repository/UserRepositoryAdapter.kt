package com.jpmns.task.core.external.persistence.repository

import java.util.UUID

import org.springframework.stereotype.Repository

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.user.UserEntity
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject
import com.jpmns.task.core.external.persistence.dao.UserJpaDao
import com.jpmns.task.core.external.persistence.mapper.UserMapper

@Repository
class UserRepositoryAdapter(
    private val dao: UserJpaDao,
) : UserRepository {
    override fun save(user: UserEntity): UserEntity {
        var model = dao.save(UserMapper.toModel(user))

        return UserMapper.toDomain(model)
    }

    override fun findById(id: IdValueObject): UserEntity? {
        val formattedId = UUID.fromString(id.asString())

        return dao.findById(formattedId).map(UserMapper::toDomain).orElse(null)
    }

    override fun findByUsername(username: UsernameValueObject): UserEntity? =
        dao.findByUsername(username.asString())?.let(UserMapper::toDomain)

    override fun existsByUsername(username: UsernameValueObject): Boolean =
        dao.existsByUsername(username.asString())

    override fun deleteById(id: IdValueObject) {
        val formattedId = UUID.fromString(id.asString())

        return dao.deleteById(formattedId)
    }
}
