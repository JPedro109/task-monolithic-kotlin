package com.jpmns.task.core.application.usecase.task

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.DeleteTaskInputDTO
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException
import com.jpmns.task.core.application.usecase.task.implementation.DeleteTaskUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class DeleteTaskUseCaseTest {
    @MockK
    lateinit var taskRepository: TaskRepository

    @InjectMockKs
    lateinit var useCase: DeleteTaskUseCaseImpl

    @Test
    fun `should delete a task successfully`() {
        val task = TaskFixture.aTask()
        val taskId = task.id
        val userId = task.userId
        val input = DeleteTaskInputDTO(
            taskId = taskId.asString(),
            userId = userId.asString()
        )

        every { taskRepository.findById(taskId) } returns task
        every { taskRepository.deleteById(taskId) } just Runs

        useCase.execute(input)

        verify { taskRepository.findById(taskId) }
        verify { taskRepository.deleteById(taskId) }
    }

    @Test
    fun `should throw when task id is invalid`() {
        val task = TaskFixture.aTask()
        val userId = task.userId
        val invalidTaskId = "invalid-id"
        val input = DeleteTaskInputDTO(
            taskId = invalidTaskId,
            userId = userId.asString()
        )

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify(exactly = 0) { taskRepository.findById(any()) }
        verify(exactly = 0) { taskRepository.deleteById(any()) }
    }

    @Test
    fun `should throw when task is not found`() {
        val task = TaskFixture.aTask()
        val taskId = task.id
        val userId = task.userId
        val input = DeleteTaskInputDTO(
            taskId = taskId.asString(),
            userId = userId.asString()
        )

        every { taskRepository.findById(taskId) } returns null

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(TaskNotFoundException::class.java)

        verify { taskRepository.findById(taskId) }
        verify(exactly = 0) { taskRepository.deleteById(any()) }
    }

    @Test
    fun `should throw when user does not own the task`() {
        val task = TaskFixture.aTask()
        val taskId = task.id
        val anotherUser = UserFixture.aUser()
        val anotherUserId = anotherUser.id
        val input = DeleteTaskInputDTO(
            taskId = taskId.asString(),
            userId = anotherUserId.asString()
        )

        every { taskRepository.findById(taskId) } returns task

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(TaskAccessDeniedException::class.java)

        verify { taskRepository.findById(taskId) }
        verify(exactly = 0) { taskRepository.deleteById(any()) }
    }
}
