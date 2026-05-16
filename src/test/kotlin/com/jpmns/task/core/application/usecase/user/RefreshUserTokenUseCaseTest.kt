package com.jpmns.task.core.application.usecase.user

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.port.security.dto.DecodeTokenDto
import com.jpmns.task.core.application.port.security.exception.InvalidTokenException
import com.jpmns.task.core.application.usecase.user.dto.input.RefreshUserTokenInputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.implementation.RefreshUserTokenUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class RefreshUserTokenUseCaseTest {
    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var token: Token

    @InjectMockKs
    lateinit var useCase: RefreshUserTokenUseCaseImpl

    @Test
    fun `should refresh token successfully`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val refreshTokenValue = "valid-refresh-token"
        val newAccessToken = "new-access-token"
        val newRefreshToken = "new-refresh-token"
        val decoded = DecodeTokenDto(sub = userId.asString())
        val input = RefreshUserTokenInputDTO(
            refreshToken = refreshTokenValue
        )

        every { token.tokenValidation(refreshTokenValue) } returns decoded
        every { userRepository.findById(userId) } returns user
        every { token.generateAccessToken(userId.asString()) } returns newAccessToken
        every { token.generateRefreshToken(userId.asString()) } returns newRefreshToken

        val output = useCase.execute(input)

        assertThat(output.accessToken).isEqualTo(newAccessToken)
        assertThat(output.refreshToken).isEqualTo(newRefreshToken)

        verify { token.tokenValidation(refreshTokenValue) }
        verify { userRepository.findById(userId) }
        verify { token.generateAccessToken(userId.asString()) }
        verify { token.generateRefreshToken(userId.asString()) }
    }

    @Test
    fun `should throw when refresh token is invalid`() {
        val refreshTokenValue = "invalid-refresh-token"
        val input = RefreshUserTokenInputDTO(
            refreshToken = refreshTokenValue
        )

        every { token.tokenValidation(refreshTokenValue) } throws InvalidTokenException()

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(InvalidTokenException::class.java)

        verify { token.tokenValidation(refreshTokenValue) }
        verify(exactly = 0) { userRepository.findById(any()) }
        verify(exactly = 0) { token.generateAccessToken(any()) }
        verify(exactly = 0) { token.generateRefreshToken(any()) }
    }

    @Test
    fun `should throw when decoded sub is not a valid id`() {
        val refreshTokenValue = "valid-refresh-token"
        val decoded = DecodeTokenDto(sub = "invalid-id")
        val input = RefreshUserTokenInputDTO(
            refreshToken = refreshTokenValue
        )

        every { token.tokenValidation(refreshTokenValue) } returns decoded

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify { token.tokenValidation(refreshTokenValue) }
        verify(exactly = 0) { userRepository.findById(any()) }
        verify(exactly = 0) { token.generateAccessToken(any()) }
        verify(exactly = 0) { token.generateRefreshToken(any()) }
    }

    @Test
    fun `should throw when user is not found`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val refreshTokenValue = "valid-refresh-token"
        val decoded = DecodeTokenDto(sub = userId.asString())
        val input = RefreshUserTokenInputDTO(
            refreshToken = refreshTokenValue
        )

        every { token.tokenValidation(refreshTokenValue) } returns decoded
        every { userRepository.findById(userId) } returns null

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(UserNotFoundException::class.java)

        verify { token.tokenValidation(refreshTokenValue) }
        verify { userRepository.findById(userId) }
        verify(exactly = 0) { token.generateAccessToken(any()) }
        verify(exactly = 0) { token.generateRefreshToken(any()) }
    }
}
