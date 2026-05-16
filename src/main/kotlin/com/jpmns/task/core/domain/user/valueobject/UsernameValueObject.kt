package com.jpmns.task.core.domain.user.valueobject

import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.core.domain.user.valueobject.exception.InvalidUsernameException
import com.jpmns.task.shared.type.Result

class UsernameValueObject private constructor(private val value: String) {
    fun asString(): String = value

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is UsernameValueObject) {
            return false
        }

        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    companion object {
        private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]{3,50}$")

        fun of(username: String): Result<UsernameValueObject, DomainException> {
            if (!USERNAME_REGEX.matches(username)) {
                return Result.fail(
                    InvalidUsernameException()
                )
            }
            return Result.success(UsernameValueObject(username))
        }
    }
}
