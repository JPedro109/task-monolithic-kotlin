package com.jpmns.task.core.external.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import com.jpmns.task.configuration.security.SecurityConfigProperties
import com.jpmns.task.core.application.port.security.exception.InvalidTokenException
import com.jpmns.task.shared.fixture.UserFixture

class TokenAdapterTest {
    private lateinit var tokenAdapter: TokenAdapter

    @BeforeEach
    fun setUp() {
        val properties = SecurityConfigProperties(
            jwt = SecurityConfigProperties.Jwt(
                secret = SECRET,
                accessTokenExpirationMs = ACCESS_EXPIRATION_MS,
                refreshTokenExpirationMs = REFRESH_EXPIRATION_MS
            )
        )
        tokenAdapter = TokenAdapter(properties)
    }

    @Test
    fun `should generate a non-null access token for a given subject`() {
        val user = UserFixture.aUser()
        val sub = user.id.asString()

        val token = tokenAdapter.generateAccessToken(sub)

        assertThat(token).isNotNull()
        assertThat(token).isNotBlank()
    }

    @Test
    fun `should generate a non-null refresh token for a given subject`() {
        val user = UserFixture.aUser()
        val sub = user.id.asString()

        val token = tokenAdapter.generateRefreshToken(sub)

        assertThat(token).isNotNull()
        assertThat(token).isNotBlank()
    }

    @Test
    fun `should generate different tokens for access and refresh`() {
        val user = UserFixture.aUser()
        val sub = user.id.asString()

        val accessToken = tokenAdapter.generateAccessToken(sub)
        val refreshToken = tokenAdapter.generateRefreshToken(sub)

        assertThat(accessToken).isNotEqualTo(refreshToken)
    }

    @Test
    fun `should validate a valid access token and return the correct subject`() {
        val user = UserFixture.aUser()
        val sub = user.id.asString()
        val token = tokenAdapter.generateAccessToken(sub)

        val decoded = tokenAdapter.tokenValidation(token)

        assertThat(decoded).isNotNull()
        assertThat(decoded.sub).isEqualTo(sub)
    }

    @Test
    fun `should validate a valid refresh token and return the correct subject`() {
        val user = UserFixture.aUser()
        val sub = user.id.asString()
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
        val user = UserFixture.aUser()
        val sub = user.id.asString()
        val otherProperties = SecurityConfigProperties(
            jwt = SecurityConfigProperties.Jwt(
                secret = ANOTHER_SECRET,
                accessTokenExpirationMs = ACCESS_EXPIRATION_MS,
                refreshTokenExpirationMs = REFRESH_EXPIRATION_MS
            )
        )
        val otherAdapter = TokenAdapter(otherProperties)
        val token = otherAdapter.generateAccessToken(sub)

        assertThatThrownBy { tokenAdapter.tokenValidation(token) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    @Test
    fun `should throw InvalidTokenException when token is expired`() {
        val user = UserFixture.aUser()
        val sub = user.id.asString()
        val expiredProperties = SecurityConfigProperties(
            jwt = SecurityConfigProperties.Jwt(
                secret = SECRET,
                accessTokenExpirationMs = -1L,
                refreshTokenExpirationMs = -1L
            )
        )
        val expiredAdapter = TokenAdapter(expiredProperties)
        val token = expiredAdapter.generateAccessToken(sub)

        assertThatThrownBy { tokenAdapter.tokenValidation(token) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    private companion object {
        const val SECRET = "test-secret-key-must-be-at-least-32-chars!!"
        const val ANOTHER_SECRET = "another-test-secret-key-must-be-at-least-32-chars!!"
        const val ACCESS_EXPIRATION_MS = 900_000L
        const val REFRESH_EXPIRATION_MS = 604_800_000L
    }
}
