package com.jpmns.task.core.application.port.persistence.repository

import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.task.TaskEntity

interface TaskRepository {
    fun save(task: TaskEntity): TaskEntity

    fun findById(id: IdValueObject): TaskEntity?

    fun findAllByUserId(userId: IdValueObject): List<TaskEntity>

    fun deleteById(id: IdValueObject)
}
