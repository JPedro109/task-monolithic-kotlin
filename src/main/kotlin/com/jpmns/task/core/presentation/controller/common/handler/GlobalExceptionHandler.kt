package com.jpmns.task.core.presentation.controller.common.handler

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
        logger.error("Validation error", ex.message, ex)

        val errors = ex.bindingResult.fieldErrors.joinToString(", ") {
            "${it.field}: ${it.defaultMessage}"
        }

        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors)

        problem.title = "Validation Failed"

        return problem
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(ex: HttpMessageNotReadableException): ProblemDetail {
        logger.error("Message not readable", ex.message, ex)

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request body is missing or malformed"
        )

        problem.title = "Bad Request"

        return problem
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomain(ex: DomainException): ProblemDetail {
        logger.error("Domain error", ex.message, ex)

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.errors.joinToString(", ")
        )

        problem.title = "Domain Error"

        return problem
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ProblemDetail {
        logger.error("User not found", ex.message, ex)

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.message ?: TITLE_NOT_FOUND
        )

        problem.title = TITLE_NOT_FOUND

        return problem
    }

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleTaskNotFound(ex: TaskNotFoundException): ProblemDetail {
        logger.error("Task not found", ex.message, ex)

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.message ?: TITLE_NOT_FOUND
        )

        problem.title = TITLE_NOT_FOUND

        return problem
    }

    @ExceptionHandler(UsernameAlreadyExistsException::class)
    fun handleUsernameConflict(ex: UsernameAlreadyExistsException): ProblemDetail {
        logger.error("Username conflict", ex.message, ex)

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.message ?: "Conflict"
        )

        problem.title = "Conflict"

        return problem
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ProblemDetail {
        logger.error("Invalid credentials", ex.message, ex)

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            ex.message ?: TITLE_UNAUTHORIZED
        )

        problem.title = TITLE_UNAUTHORIZED

        return problem
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(ex: InvalidTokenException): ProblemDetail {
        logger.error("Invalid token", ex.message, ex)

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            ex.message ?: TITLE_UNAUTHORIZED
        )

        problem.title = TITLE_UNAUTHORIZED

        return problem
    }

    @ExceptionHandler(TaskAccessDeniedException::class)
    fun handleTaskAccessDenied(ex: TaskAccessDeniedException): ProblemDetail {
        logger.error("Task access denied", ex.message, ex)

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            ex.message ?: "Forbidden"
        )

        problem.title = "Forbidden"

        return problem
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ProblemDetail {
        logger.error("Internal server error", ex.message, ex)

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error"
        )

        problem.title = "Internal Server Error"

        return problem
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

        const val TITLE_NOT_FOUND = "Not Found"
        const val TITLE_UNAUTHORIZED = "Unauthorized"
    }
}
