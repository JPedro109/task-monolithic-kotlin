package com.jpmns.task.core.external.security

import java.util.Date
import javax.crypto.SecretKey

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import com.jpmns.task.configuration.security.SecurityConfigProperties
import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.port.security.TokenType
import com.jpmns.task.core.application.port.security.dto.DecodeTokenDto
import com.jpmns.task.core.application.port.security.exception.InvalidTokenException

import io.jsonwebtoken.Claims
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
        buildToken(sub, TokenType.ACCESS, properties.jwt.accessTokenExpirationMs)

    override fun generateRefreshToken(sub: String): String =
        buildToken(sub, TokenType.REFRESH, properties.jwt.refreshTokenExpirationMs)

    override fun validateAccessToken(token: String): DecodeTokenDto =
        validateToken(token, TokenType.ACCESS)

    override fun validateRefreshToken(token: String): DecodeTokenDto =
        validateToken(token, TokenType.REFRESH)

    private fun validateToken(token: String, expectedType: TokenType): DecodeTokenDto = runCatching {
        val claims = parseClaims(token)

        val tokenType = TokenType.fromValue(claims[CLAIM_TOKEN_TYPE, String::class.java])

        if (tokenType != expectedType) {
            throw InvalidTokenException()
        }

        return DecodeTokenDto(sub = claims.subject)
    }.getOrElse {
        logger.warn("Invalid JWT token: ${it.message}")

        throw InvalidTokenException()
    }

    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload

    private fun buildToken(sub: String, tokenType: TokenType, expirationMs: Long): String {
        val now = Date()

        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(sub)
            .claim(CLAIM_TOKEN_TYPE, tokenType.value)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(TokenAdapter::class.java)

        const val CLAIM_TOKEN_TYPE = "token_type"
    }
}
