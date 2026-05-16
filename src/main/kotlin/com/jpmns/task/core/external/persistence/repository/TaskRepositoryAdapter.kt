package com.jpmns.task.core.external.persistence.repository

import java.util.UUID

import org.springframework.stereotype.Repository

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.task.TaskEntity
import com.jpmns.task.core.external.persistence.dao.TaskJpaDao
import com.jpmns.task.core.external.persistence.mapper.TaskMapper

@Repository
class TaskRepositoryAdapter(
    private val dao: TaskJpaDao
) : TaskRepository {
    override fun save(task: TaskEntity): TaskEntity {
        val model = dao.save(TaskMapper.toModel(task))

        return TaskMapper.toDomain(model)
    }

    override fun findById(id: IdValueObject): TaskEntity? {
        val formattedId = UUID.fromString(id.asString())

        return dao.findById(formattedId).map(TaskMapper::toDomain).orElse(null)
    }

    override fun findAllByUserId(userId: IdValueObject): List<TaskEntity> {
        val formattedUserId = UUID.fromString(userId.asString())

        return dao.findAllByUserId(formattedUserId).map(TaskMapper::toDomain)
    }

    override fun deleteById(id: IdValueObject) {
        val formattedId = UUID.fromString(id.asString())

        dao.deleteById(formattedId)
    }
}
