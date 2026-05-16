package com.jpmns.task.core.domain.task

import java.time.Instant

import com.jpmns.task.core.domain.common.abstracts.Entity
import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.task.valueobject.TaskNameValueObject

class TaskEntity(
    id: String,
    userId: String,
    taskName: String,
    finished: Boolean,
    createdAt: Instant? = null,
    val updatedAt: Instant? = null
) : Entity(id, createdAt) {
    val userId: IdValueObject

    var taskName: TaskNameValueObject
        private set

    var finished: Boolean
        private set

    init {
        val userIdResult = IdValueObject.of(userId)
        val taskNameResult = TaskNameValueObject.of(taskName)
        validateOrThrow(listOf(userIdResult, taskNameResult))

        this.userId = userIdResult.getSuccessValue()
        this.taskName = taskNameResult.getSuccessValue()
        this.finished = finished
    }

    fun updateTaskName(taskName: String) {
        val result = TaskNameValueObject.of(taskName)

        validateOrThrow(listOf(result))

        this.taskName = result.getSuccessValue()
    }

    fun markAsFinished() {
        finished = true
    }
}
