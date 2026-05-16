package com.jpmns.task.shared.type

sealed class Result<out T, out E> {
    data class Success<T>(val value: T) : Result<T, Nothing>()

    data class Failure<E>(val error: E) : Result<Nothing, E>()

    val isFail: Boolean
        get() = this is Failure

    fun getSuccessValue(): T = (this as Success).value

    fun getFailureError(): E = (this as Failure).error

    companion object {
        fun <T, E> success(value: T): Result<T, E> = Success(value)

        fun <T, E> fail(error: E): Result<T, E> = Failure(error)
    }
}
