package com.jpmns.task.shared.type

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()

    data class Failure(val error: RuntimeException) : Result<Nothing>()

    val isFailure: Boolean
        get() = this is Failure

    fun getValueResult(): T = when (this) {
        is Success -> value

        is Failure -> error(
            "The result is a failure, value does not exist"
        )
    }

    fun getValueResultOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error
    }

    fun getErrorResult(): RuntimeException = when (this) {
        is Success -> error(
            "The result is a success, error does not exist"
        )

        is Failure -> error
    }

    companion object {
        fun <T> success(value: T): Result<T> = Success(value)

        fun fail(error: RuntimeException): Result<Nothing> = Failure(error)
    }
}
