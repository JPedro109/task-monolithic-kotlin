package com.jpmns.task.core.domain.user.valueobject

import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.core.domain.user.valueobject.exception.InvalidPasswordException
import com.jpmns.task.shared.type.Result

class UserPasswordValueObject private constructor(private val value: String) {
    fun asString(): String = value

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is UserPasswordValueObject) {
            return false
        }

        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    companion object {
        fun of(password: String): Result<UserPasswordValueObject, DomainException> {
            if (password.isEmpty()) {
                return Result.fail(InvalidPasswordException())
            }

            return Result.success(UserPasswordValueObject(password))
        }
    }
}
