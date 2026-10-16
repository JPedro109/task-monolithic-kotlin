package com.jpmns.task.core.application.port.security

import com.jpmns.task.core.application.port.security.exception.InvalidTokenException

enum class TokenType(val value: String) {
    ACCESS("access"),
    REFRESH("refresh");

    companion object {
        fun fromValue(value: String?): TokenType =
            entries.find { it.value == value } ?: throw InvalidTokenException()
    }
}
