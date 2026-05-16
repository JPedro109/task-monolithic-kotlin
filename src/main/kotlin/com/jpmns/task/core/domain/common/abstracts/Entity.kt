package com.jpmns.task.core.domain.common.abstracts

import java.time.Instant

import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.shared.type.Result

abstract class Entity(id: String, createdAt: Instant? = null) {
    val id: IdValueObject
    val createdAt: Instant

    init {
        val idResult = IdValueObject.of(id)
        validateOrThrow(listOf(idResult))

        this.id = idResult.getRealValue()
        this.createdAt = createdAt ?: Instant.now()
    }

    protected fun validateOrThrow(results: List<Result<*, DomainException>>) {
        val errors = results.filter { it.isFail }.map { it.getRealError() }

        if (errors.isNotEmpty()) {
            throw DomainException.with(errors)
        }
    }
}
