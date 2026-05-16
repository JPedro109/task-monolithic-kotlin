package com.jpmns.task.core.application.usecase.task

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.ListTasksInputDTO
import com.jpmns.task.core.application.usecase.task.implementation.ListTasksUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class ListTasksUseCaseTest {
    @MockK
    lateinit var taskRepository: TaskRepository

    @InjectMockKs
    lateinit var useCase: ListTasksUseCaseImpl

    @Test
    fun `should list tasks successfully`() {
        val task = TaskFixture.aTask()
        val user = UserFixture.aUser()
        val userId = user.id
        val input = ListTasksInputDTO(
            userId = userId.asString()
        )

        every { taskRepository.findAllByUserId(userId) } returns listOf(task)

        val output = useCase.execute(input)

        assertThat(output).hasSize(1)
        assertThat(output[0].id).isEqualTo(task.id.asString())
        assertThat(output[0].userId).isEqualTo(task.userId.asString())
        assertThat(output[0].taskName).isEqualTo(task.taskName.asString())
        assertThat(output[0].finished).isEqualTo(task.finished)
        assertThat(output[0].createdAt).isEqualTo(task.createdAt)

        verify { taskRepository.findAllByUserId(userId) }
    }

    @Test
    fun `should return empty list when user has no tasks`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val input = ListTasksInputDTO(
            userId = userId.asString()
        )

        every { taskRepository.findAllByUserId(userId) } returns emptyList()

        val output = useCase.execute(input)

        assertThat(output).isEmpty()

        verify { taskRepository.findAllByUserId(userId) }
    }

    @Test
    fun `should throw when user id is invalid`() {
        val invalidUserId = "invalid-id"
        val input = ListTasksInputDTO(
            userId = invalidUserId
        )

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify(exactly = 0) { taskRepository.findAllByUserId(any()) }
    }
}
