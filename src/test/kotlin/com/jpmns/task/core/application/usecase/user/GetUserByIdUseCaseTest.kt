package com.jpmns.task.core.application.usecase.user

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.implementation.GetUserByIdUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class GetUserByIdUseCaseTest {
    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var useCase: GetUserByIdUseCaseImpl

    @Test
    fun `should get user by id successfully`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val input = GetUserByIdInputDTO(
            id = userId.asString()
        )

        every { userRepository.findById(userId) } returns user

        val output = useCase.execute(input)

        assertThat(output.id).isEqualTo(userId.asString())
        assertThat(output.username).isEqualTo(user.username.asString())
        assertThat(output.createdAt).isEqualTo(user.createdAt)
        assertThat(output.updatedAt).isEqualTo(user.updatedAt)

        verify { userRepository.findById(userId) }
    }

    @Test
    fun `should throw when user id is invalid`() {
        val invalidId = "invalid-id"
        val input = GetUserByIdInputDTO(
            id = invalidId
        )

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify(exactly = 0) { userRepository.findById(any()) }
    }

    @Test
    fun `should throw when user is not found`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val input = GetUserByIdInputDTO(
            id = userId.asString()
        )

        every { userRepository.findById(userId) } returns null

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(UserNotFoundException::class.java)

        verify { userRepository.findById(userId) }
    }
}
