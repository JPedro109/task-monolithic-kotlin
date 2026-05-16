package com.jpmns.task.core.external.persistence.dao

import java.util.UUID

import org.springframework.data.jpa.repository.JpaRepository

import com.jpmns.task.core.external.persistence.model.UserJpaModel

interface UserJpaDao : JpaRepository<UserJpaModel, UUID> {
    fun findByUsername(username: String): UserJpaModel?

    fun existsByUsername(username: String): Boolean
}
