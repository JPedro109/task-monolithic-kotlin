package com.jpmns.task.core.presentation.controller.common.handler

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

import com.jpmns.task.core.application.port.security.exception.InvalidTokenException
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException
import com.jpmns.task.core.domain.common.exception.DomainException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ProblemDetail {
        logger.error("Validation error: ${ex.message}", ex)

        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
            .ifBlank { "Validation error" }

        return buildProblemDetail(HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(ex: HttpMessageNotReadableException): ProblemDetail {
        logger.error("Message not readable: ${ex.message}", ex)

        return buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Request body is missing or malformed"
        )
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomain(ex: DomainException): ProblemDetail {
        logger.error("Domain exception: ${ex.errors}", ex)

        return buildProblemDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.errors.joinToString(", ")
        )
    }

    @ExceptionHandler(UserNotFoundException::class, TaskNotFoundException::class)
    fun handleNotFound(ex: Exception): ProblemDetail {
        logger.error("Resource not found: ${ex.message}", ex)

        return buildProblemDetail(
            HttpStatus.NOT_FOUND,
            ex.message ?: "Resource not found"
        )
    }

    @ExceptionHandler(UsernameAlreadyExistsException::class)
    fun handleConflict(ex: Exception): ProblemDetail {
        logger.error("Conflict error: ${ex.message}", ex)

        return buildProblemDetail(
            HttpStatus.CONFLICT,
            ex.message ?: "Conflict"
        )
    }

    @ExceptionHandler(
        InvalidCredentialsException::class,
        InvalidTokenException::class
    )
    fun handleUnauthorized(ex: Exception): ProblemDetail {
        logger.error("Unauthorized access: ${ex.message}", ex)

        return buildProblemDetail(
            HttpStatus.UNAUTHORIZED,
            ex.message ?: "Unauthorized"
        )
    }

    @ExceptionHandler(TaskAccessDeniedException::class)
    fun handleForbidden(ex: Exception): ProblemDetail {
        logger.error("Access denied: ${ex.message}", ex)

        return buildProblemDetail(
            HttpStatus.FORBIDDEN,
            ex.message ?: "Forbidden"
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ProblemDetail {
        logger.error("Unexpected error: ${ex.message}", ex)

        return buildProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error"
        )
    }

    private fun buildProblemDetail(status: HttpStatus, message: String): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatusCode.valueOf(status.value()),
            message
        )

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}
