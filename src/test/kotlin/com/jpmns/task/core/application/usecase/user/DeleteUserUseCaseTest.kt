package com.jpmns.task.core.application.usecase.user

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.implementation.DeleteUserUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class DeleteUserUseCaseTest {
    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var useCase: DeleteUserUseCaseImpl

    @Test
    fun `should delete a user successfully`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val input = DeleteUserInputDTO(
            userId = userId.asString()
        )

        every { userRepository.findById(userId) } returns user
        every { userRepository.deleteById(userId) } just Runs

        useCase.execute(input)

        verify { userRepository.findById(userId) }
        verify { userRepository.deleteById(userId) }
    }

    @Test
    fun `should throw when user id is invalid`() {
        val invalidUserId = "invalid-id"
        val input = DeleteUserInputDTO(
            userId = invalidUserId
        )

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify(exactly = 0) { userRepository.findById(any()) }
        verify(exactly = 0) { userRepository.deleteById(any()) }
    }

    @Test
    fun `should throw when user is not found`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val input = DeleteUserInputDTO(
            userId = userId.asString()
        )

        every { userRepository.findById(userId) } returns null

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(UserNotFoundException::class.java)

        verify { userRepository.findById(userId) }
        verify(exactly = 0) { userRepository.deleteById(any()) }
    }
}
