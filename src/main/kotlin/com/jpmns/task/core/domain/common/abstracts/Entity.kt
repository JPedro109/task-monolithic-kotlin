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

        val results = listOf(idResult)
        validateOrThrow(results)

        this.id = idResult.getValueResult()
        this.createdAt = createdAt ?: Instant.now()
    }

    protected fun validateOrThrow(results: List<Result<*>>) {
        val errors = results.filter { it.isFailure }.map { it.getErrorResult() as DomainException }

        if (errors.isNotEmpty()) {
            throw DomainException.with(errors)
        }
    }
}
