package com.jpmns.task.core.application.usecase.user

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.port.security.PasswordEncoder
import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.usecase.user.dto.input.UserLoginInputDTO
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException
import com.jpmns.task.core.application.usecase.user.implementation.UserLoginUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class UserLoginUseCaseTest {
    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @MockK
    lateinit var token: Token

    @InjectMockKs
    lateinit var useCase: UserLoginUseCaseImpl

    @Test
    fun `should login successfully`() {
        val user = UserFixture.aUser()
        val username = user.username
        val password = user.password
        val userId = user.id
        val usernameString = username.asString()
        val passwordString = password.asString()
        val userIdString = userId.asString()
        val accessToken = "generated-access-token"
        val refreshToken = "generated-refresh-token"
        val input = UserLoginInputDTO(
            username = usernameString,
            password = passwordString
        )

        every { userRepository.findByUsername(username) } returns user
        every { passwordEncoder.matches(rawPassword = passwordString, encodedPassword = passwordString) } returns true
        every { token.generateAccessToken(userIdString) } returns accessToken
        every { token.generateRefreshToken(userIdString) } returns refreshToken

        val output = useCase.execute(input)

        assertThat(output.accessToken).isEqualTo(accessToken)
        assertThat(output.refreshToken).isEqualTo(refreshToken)
        verify { userRepository.findByUsername(username) }
        verify { passwordEncoder.matches(rawPassword = passwordString, encodedPassword = passwordString) }
        verify { token.generateAccessToken(userIdString) }
        verify { token.generateRefreshToken(userIdString) }
    }

    @Test
    fun `should throw when user is not found by username`() {
        val user = UserFixture.aUser()
        val username = user.username
        val password = user.password
        val usernameString = username.asString()
        val passwordString = password.asString()
        val input = UserLoginInputDTO(
            username = usernameString,
            password = passwordString
        )

        every { userRepository.findByUsername(username) } returns null

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(InvalidCredentialsException::class.java)
        verify { userRepository.findByUsername(username) }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
        verify(exactly = 0) { token.generateAccessToken(any()) }
        verify(exactly = 0) { token.generateRefreshToken(any()) }
    }

    @Test
    fun `should throw when password does not match`() {
        val user = UserFixture.aUser()
        val username = user.username
        val password = user.password
        val usernameString = username.asString()
        val passwordString = password.asString()
        val wrongPassword = "wrong-password"
        val input = UserLoginInputDTO(
            username = usernameString,
            password = wrongPassword
        )

        every { userRepository.findByUsername(username) } returns user
        every { passwordEncoder.matches(rawPassword = wrongPassword, encodedPassword = passwordString) } returns false

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(InvalidCredentialsException::class.java)
        verify { userRepository.findByUsername(username) }
        verify { passwordEncoder.matches(rawPassword = wrongPassword, encodedPassword = passwordString) }
        verify(exactly = 0) { token.generateAccessToken(any()) }
        verify(exactly = 0) { token.generateRefreshToken(any()) }
    }

    @Test
    fun `should throw when username is invalid`() {
        val user = UserFixture.aUser()
        val password = user.password
        val passwordString = password.asString()
        val invalidUsername = "ab"
        val input = UserLoginInputDTO(
            username = invalidUsername,
            password = passwordString
        )

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)
        verify(exactly = 0) { userRepository.findByUsername(any()) }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
        verify(exactly = 0) { token.generateAccessToken(any()) }
        verify(exactly = 0) { token.generateRefreshToken(any()) }
    }
}
