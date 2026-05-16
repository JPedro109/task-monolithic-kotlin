package com.jpmns.task.core.application.port.security

import com.jpmns.task.core.application.port.security.dto.DecodeTokenDto

interface Token {
    fun generateAccessToken(sub: String): String

    fun generateRefreshToken(sub: String): String

    fun tokenValidation(token: String): DecodeTokenDto
}
