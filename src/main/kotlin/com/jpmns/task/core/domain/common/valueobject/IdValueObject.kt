package com.jpmns.task.core.domain.common.valueobject

import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.core.domain.common.valueobject.exception.InvalidIdValueObjectException
import com.jpmns.task.shared.type.Result

class IdValueObject private constructor(private val value: String) {
    fun asString(): String = value

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is IdValueObject) {
            return false
        }

        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    companion object {
        private val UUID_REGEX =
            Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

        fun of(id: String): Result<IdValueObject, DomainException> {
            if (!UUID_REGEX.matches(id)) {
                return Result.fail(InvalidIdValueObjectException())
            }

            return Result.success(IdValueObject(id))
        }
    }
}
