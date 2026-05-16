package com.jpmns.task.core.domain.task.valueobject

import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.core.domain.task.valueobject.exception.InvalidTaskNameException
import com.jpmns.task.shared.type.Result

class TaskNameValueObject private constructor(private val value: String) {
    fun asString(): String = value

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is TaskNameValueObject) {
            return false
        }

        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    companion object {
        private const val MAX_TASK_NAME_LENGTH = 255

        fun of(taskName: String): Result<TaskNameValueObject, DomainException> {
            if (taskName.length > MAX_TASK_NAME_LENGTH) {
                return Result.fail(
                    InvalidTaskNameException(),
                )
            }
            return Result.success(TaskNameValueObject(taskName))
        }
    }
}
