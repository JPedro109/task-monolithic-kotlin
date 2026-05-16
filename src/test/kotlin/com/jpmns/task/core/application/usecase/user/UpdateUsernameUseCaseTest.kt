package com.jpmns.task.core.application.usecase.user

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUsernameInputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException
import com.jpmns.task.core.application.usecase.user.implementation.UpdateUsernameUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class UpdateUsernameUseCaseTest {
    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var useCase: UpdateUsernameUseCaseImpl

    @Test
    fun `should update username successfully`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val newUsername = "newusername"
        val input = UpdateUsernameInputDTO(
            userId = userId.asString(),
            newUsername = newUsername
        )

        every { userRepository.findById(userId) } returns user
        every { userRepository.existsByUsername(any()) } returns false
        every { userRepository.save(any()) } returns user

        val output = useCase.execute(input)

        assertThat(output.id).isEqualTo(userId.asString())

        verify { userRepository.findById(userId) }
        verify { userRepository.existsByUsername(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `should throw when user id is invalid`() {
        val invalidUserId = "invalid-id"
        val newUsername = "newusername"
        val input = UpdateUsernameInputDTO(
            userId = invalidUserId,
            newUsername = newUsername
        )

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify(exactly = 0) { userRepository.findById(any()) }
        verify(exactly = 0) { userRepository.existsByUsername(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should throw when user is not found`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val newUsername = "newusername"
        val input = UpdateUsernameInputDTO(
            userId = userId.asString(),
            newUsername = newUsername
        )

        every { userRepository.findById(userId) } returns null

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(UserNotFoundException::class.java)

        verify { userRepository.findById(userId) }
        verify(exactly = 0) { userRepository.existsByUsername(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should throw when new username is invalid`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val invalidUsername = "ab"
        val input = UpdateUsernameInputDTO(
            userId = userId.asString(),
            newUsername = invalidUsername
        )

        every { userRepository.findById(userId) } returns user

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify { userRepository.findById(userId) }
        verify(exactly = 0) { userRepository.existsByUsername(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should throw when new username already exists`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val newUsername = "existing_user"
        val input = UpdateUsernameInputDTO(
            userId = userId.asString(),
            newUsername = newUsername
        )

        every { userRepository.findById(userId) } returns user
        every { userRepository.existsByUsername(any()) } returns true

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(UsernameAlreadyExistsException::class.java)

        verify { userRepository.findById(userId) }
        verify { userRepository.existsByUsername(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }
}
