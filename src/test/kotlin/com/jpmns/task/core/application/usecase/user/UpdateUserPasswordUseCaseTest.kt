package com.jpmns.task.core.application.usecase.user

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.port.security.PasswordEncoder
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUserPasswordInputDTO
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.implementation.UpdateUserPasswordUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class UpdateUserPasswordUseCaseTest {
    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @InjectMockKs
    lateinit var useCase: UpdateUserPasswordUseCaseImpl

    @Test
    fun `should update password successfully`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val password = user.password
        val newPassword = "new-password"
        val encodedNewPassword = "encoded-password"
        val input = UpdateUserPasswordInputDTO(
            userId = userId.asString(),
            currentPassword = password.asString(),
            newPassword = newPassword
        )

        every { userRepository.findById(userId) } returns user
        every { passwordEncoder.matches(password.asString(), password.asString()) } returns true
        every { passwordEncoder.encode(newPassword) } returns encodedNewPassword
        every { userRepository.save(any()) } returns user

        useCase.execute(input)

        verify { userRepository.findById(userId) }
        verify { passwordEncoder.matches(password.asString(), password.asString()) }
        verify { passwordEncoder.encode(newPassword) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `should throw when user id is invalid`() {
        val user = UserFixture.aUser()
        val password = user.password
        val invalidUserId = "invalid-id"
        val newPassword = "new-password"
        val input = UpdateUserPasswordInputDTO(
            userId = invalidUserId,
            currentPassword = password.asString(),
            newPassword = newPassword
        )

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify(exactly = 0) { userRepository.findById(any()) }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should throw when user is not found`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val password = user.password
        val newPassword = "new-password"
        val input = UpdateUserPasswordInputDTO(
            userId = userId.asString(),
            currentPassword = password.asString(),
            newPassword = newPassword
        )

        every { userRepository.findById(userId) } returns null

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(UserNotFoundException::class.java)

        verify { userRepository.findById(userId) }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should throw when current password does not match`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val password = user.password
        val wrongPassword = "wrong-password"
        val newPassword = "new-password"
        val input = UpdateUserPasswordInputDTO(
            userId = userId.asString(),
            currentPassword = wrongPassword,
            newPassword = newPassword
        )

        every { userRepository.findById(userId) } returns user
        every { passwordEncoder.matches(wrongPassword, password.asString()) } returns false

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(InvalidCredentialsException::class.java)

        verify { userRepository.findById(userId) }
        verify { passwordEncoder.matches(wrongPassword, password.asString()) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }
}
