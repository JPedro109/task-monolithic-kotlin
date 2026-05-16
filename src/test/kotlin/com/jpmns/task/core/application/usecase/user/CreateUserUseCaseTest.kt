package com.jpmns.task.core.application.usecase.user

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.port.security.PasswordEncoder
import com.jpmns.task.core.application.usecase.user.dto.input.CreateUserInputDTO
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException
import com.jpmns.task.core.application.usecase.user.implementation.CreateUserUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class CreateUserUseCaseTest {
    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @InjectMockKs
    lateinit var useCase: CreateUserUseCaseImpl

    @Test
    fun `should create a user successfully`() {
        val user = UserFixture.aUser()
        val username = user.username
        val password = user.password
        val input = CreateUserInputDTO(
            username = username.asString(),
            password = password.asString()
        )

        every { userRepository.existsByUsername(username) } returns false
        every { passwordEncoder.encode(password.asString()) } returns password.asString()
        every { userRepository.save(any()) } returns user

        val output = useCase.execute(input)

        assertThat(output.id).isEqualTo(user.id.asString())
        assertThat(output.username).isEqualTo(username.asString())

        verify { userRepository.existsByUsername(username) }
        verify { passwordEncoder.encode(password.asString()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `should throw when username already exists`() {
        val user = UserFixture.aUser()
        val username = user.username
        val password = user.password
        val input = CreateUserInputDTO(
            username = username.asString(),
            password = password.asString()
        )

        every { userRepository.existsByUsername(username) } returns true

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(UsernameAlreadyExistsException::class.java)

        verify { userRepository.existsByUsername(username) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should throw when username is invalid`() {
        val user = UserFixture.aUser()
        val password = user.password
        val invalidUsername = "ab"
        val input = CreateUserInputDTO(
            username = invalidUsername,
            password = password.asString()
        )

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify(exactly = 0) { userRepository.existsByUsername(any()) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }
}
