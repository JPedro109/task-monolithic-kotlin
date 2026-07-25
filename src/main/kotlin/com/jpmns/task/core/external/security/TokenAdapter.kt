package com.jpmns.task.core.external.security

import java.util.Date
import javax.crypto.SecretKey

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import com.jpmns.task.configuration.security.SecurityConfigProperties
import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.port.security.dto.DecodeTokenDto
import com.jpmns.task.core.application.port.security.exception.InvalidTokenException

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys

@Component
class TokenAdapter(
    private val properties: SecurityConfigProperties
) : Token {
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(properties.jwt.secret.toByteArray())
    }

    override fun generateAccessToken(sub: String): String =
        buildToken(sub, TOKEN_TYPE_ACCESS, properties.jwt.accessTokenExpirationMs)

    override fun generateRefreshToken(sub: String): String =
        buildToken(sub, TOKEN_TYPE_REFRESH, properties.jwt.refreshTokenExpirationMs)

    override fun tokenValidation(token: String): DecodeTokenDto = runCatching {
        val claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
        return DecodeTokenDto(sub = claims.subject)
    }.getOrElse {
        logger.error("Invalid token JWT ${it.message}", it)
        throw InvalidTokenException()
    }

    private fun buildToken(sub: String, tokenType: String, expirationMs: Long): String {
        val now = Date()

        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(sub)
            .claim(CLAIM_TOKEN_TYPE, tokenType)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(TokenAdapter::class.java)

        const val CLAIM_TOKEN_TYPE = "token_type"
        const val TOKEN_TYPE_ACCESS = "access"
        const val TOKEN_TYPE_REFRESH = "refresh"
    }
}
