package com.jpmns.task.core.domain.common.exception

open class DomainException protected constructor(message: String) : RuntimeException(message) {
    open val errors: List<String> = listOf(message)

    companion object {
        fun with(causes: List<DomainException>): DomainException =
            object : DomainException("Found ${causes.size} domain errors") {
                override val errors = causes.map { it.message ?: "" }
            }
    }
}
