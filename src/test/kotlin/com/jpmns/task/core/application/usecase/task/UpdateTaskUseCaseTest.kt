package com.jpmns.task.core.application.usecase.task

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.UpdateTaskInputDTO
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException
import com.jpmns.task.core.application.usecase.task.implementation.UpdateTaskUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class UpdateTaskUseCaseTest {
    @MockK
    lateinit var taskRepository: TaskRepository

    @InjectMockKs
    lateinit var useCase: UpdateTaskUseCaseImpl

    @Test
    fun `should update a task successfully`() {
        val task = TaskFixture.aTask()
        val taskId = task.id
        val userId = task.userId
        val newTaskName = "Updated task name"
        val updatedTask = TaskFixture.aTaskWithName(newTaskName)
        val input = UpdateTaskInputDTO(
            taskId = taskId.asString(),
            userId = userId.asString(),
            taskName = newTaskName
        )

        every { taskRepository.findById(taskId) } returns task
        every { taskRepository.save(any()) } returns updatedTask

        val output = useCase.execute(input)

        assertThat(output.id).isEqualTo(updatedTask.id.asString())
        assertThat(output.userId).isEqualTo(updatedTask.userId.asString())
        assertThat(output.taskName).isEqualTo(newTaskName)
        assertThat(output.finished).isEqualTo(updatedTask.finished)
        assertThat(output.createdAt).isEqualTo(updatedTask.createdAt)

        verify { taskRepository.findById(taskId) }
        verify { taskRepository.save(any()) }
    }

    @Test
    fun `should throw when task id is invalid`() {
        val task = TaskFixture.aTask()
        val userId = task.userId
        val invalidTaskId = "invalid-id"
        val newTaskName = "New name"
        val input = UpdateTaskInputDTO(
            taskId = invalidTaskId,
            userId = userId.asString(),
            taskName = newTaskName
        )

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify(exactly = 0) { taskRepository.findById(any()) }
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `should throw when task is not found`() {
        val task = TaskFixture.aTask()
        val taskId = task.id
        val userId = task.userId
        val newTaskName = "New name"
        val input = UpdateTaskInputDTO(
            taskId = taskId.asString(),
            userId = userId.asString(),
            taskName = newTaskName
        )

        every { taskRepository.findById(taskId) } returns null

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(TaskNotFoundException::class.java)

        verify { taskRepository.findById(taskId) }
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `should throw when user does not own the task`() {
        val task = TaskFixture.aTask()
        val taskId = task.id
        val anotherUser = UserFixture.aUser()
        val anotherUserId = anotherUser.id
        val newTaskName = "New name"
        val input = UpdateTaskInputDTO(
            taskId = taskId.asString(),
            userId = anotherUserId.asString(),
            taskName = newTaskName
        )

        every { taskRepository.findById(taskId) } returns task

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(TaskAccessDeniedException::class.java)

        verify { taskRepository.findById(taskId) }
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `should throw when new task name exceeds max length`() {
        val task = TaskFixture.aTask()
        val taskId = task.id
        val userId = task.userId
        val longTaskName = "a".repeat(256)
        val input = UpdateTaskInputDTO(
            taskId = taskId.asString(),
            userId = userId.asString(),
            taskName = longTaskName
        )

        every { taskRepository.findById(taskId) } returns task

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)

        verify { taskRepository.findById(taskId) }
        verify(exactly = 0) { taskRepository.save(any()) }
    }
}
