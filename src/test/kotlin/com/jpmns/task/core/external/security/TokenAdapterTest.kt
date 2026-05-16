package com.jpmns.task.core.external.security

import java.util.UUID

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import com.jpmns.task.core.application.port.security.exception.InvalidTokenException

class TokenAdapterTest {
    private lateinit var tokenAdapter: TokenAdapter

    @BeforeEach
    fun setUp() {
        tokenAdapter = TokenAdapter(SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS)
    }

    @Test
    fun `should generate a non-null access token for a given subject`() {
        val sub = UUID.randomUUID().toString()

        val token = tokenAdapter.generateAccessToken(sub)

        assertThat(token).isNotNull()
        assertThat(token).isNotBlank()
    }

    @Test
    fun `should generate a non-null refresh token for a given subject`() {
        val sub = UUID.randomUUID().toString()

        val token = tokenAdapter.generateRefreshToken(sub)

        assertThat(token).isNotNull()
        assertThat(token).isNotBlank()
    }

    @Test
    fun `should generate different tokens for access and refresh`() {
        val sub = UUID.randomUUID().toString()

        val accessToken = tokenAdapter.generateAccessToken(sub)
        val refreshToken = tokenAdapter.generateRefreshToken(sub)

        assertThat(accessToken).isNotEqualTo(refreshToken)
    }

    @Test
    fun `should validate a valid access token and return the correct subject`() {
        val sub = UUID.randomUUID().toString()
        val token = tokenAdapter.generateAccessToken(sub)

        val decoded = tokenAdapter.tokenValidation(token)

        assertThat(decoded).isNotNull()
        assertThat(decoded.sub).isEqualTo(sub)
    }

    @Test
    fun `should validate a valid refresh token and return the correct subject`() {
        val sub = UUID.randomUUID().toString()
        val token = tokenAdapter.generateRefreshToken(sub)

        val decoded = tokenAdapter.tokenValidation(token)

        assertThat(decoded).isNotNull()
        assertThat(decoded.sub).isEqualTo(sub)
    }

    @Test
    fun `should throw InvalidTokenException when token is malformed`() {
        val malformed = "this.is.not.a.valid.jwt"

        assertThatThrownBy { tokenAdapter.tokenValidation(malformed) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    @Test
    fun `should throw InvalidTokenException when token is empty`() {
        val emptyToken = ""

        assertThatThrownBy { tokenAdapter.tokenValidation(emptyToken) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    @Test
    fun `should throw InvalidTokenException when token is signed with a different secret`() {
        val otherAdapter = TokenAdapter(
            "another-secret-key-must-be-at-least-32-chars!",
            ACCESS_EXPIRATION_MS,
            REFRESH_EXPIRATION_MS,
        )
        val sub = UUID.randomUUID().toString()
        val token = otherAdapter.generateAccessToken(sub)

        assertThatThrownBy { tokenAdapter.tokenValidation(token) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    @Test
    fun `should throw InvalidTokenException when token is expired`() {
        val expiredAdapter = TokenAdapter(SECRET, -1L, -1L)
        val sub = UUID.randomUUID().toString()
        val token = expiredAdapter.generateAccessToken(sub)

        assertThatThrownBy { tokenAdapter.tokenValidation(token) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    private companion object {
        const val SECRET = "test-secret-key-must-be-at-least-32-chars!!"
        const val ACCESS_EXPIRATION_MS = 900_000L
        const val REFRESH_EXPIRATION_MS = 604_800_000L
    }
}
