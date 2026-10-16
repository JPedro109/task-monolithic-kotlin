package com.jpmns.task.core.application.port.security

import com.jpmns.task.core.application.port.security.dto.DecodeTokenDto

interface Token {
    fun generateAccessToken(sub: String): String

    fun generateRefreshToken(sub: String): String

    fun validateAccessToken(token: String): DecodeTokenDto

    fun validateRefreshToken(token: String): DecodeTokenDto
}
