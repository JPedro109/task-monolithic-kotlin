package com.jpmns.task.core.external.persistence.dao

import java.util.UUID

import org.springframework.data.jpa.repository.JpaRepository

import com.jpmns.task.core.external.persistence.model.TaskJpaModel

interface TaskJpaDao : JpaRepository<TaskJpaModel, UUID> {
    fun findAllByUserId(userId: UUID): List<TaskJpaModel>
}
