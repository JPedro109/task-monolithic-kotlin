package com.jpmns.task.core.application.usecase.task

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDTO
import com.jpmns.task.core.application.usecase.task.implementation.CreateTaskUseCaseImpl
import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class CreateTaskUseCaseTest {
    @MockK
    lateinit var taskRepository: TaskRepository

    @InjectMockKs
    lateinit var useCase: CreateTaskUseCaseImpl

    @Test
    fun `should create a task successfully`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val task = TaskFixture.aTaskWithUserId(userId = userId.asString())
        val taskId = task.id
        val taskName = task.taskName
        val finished = task.finished
        val createdAt = task.createdAt
        val input = CreateTaskInputDTO(
            userId = userId.asString(),
            taskName = taskName.asString()
        )

        every { taskRepository.save(any()) } returns task

        val output = useCase.execute(input)

        assertThat(output.id).isEqualTo(taskId.asString())
        assertThat(output.userId).isEqualTo(userId.asString())
        assertThat(output.taskName).isEqualTo(taskName.asString())
        assertThat(output.finished).isEqualTo(finished)
        assertThat(output.createdAt).isEqualTo(createdAt)
        verify { taskRepository.save(any()) }
    }

    @Test
    fun `should throw when task name is invalid`() {
        val user = UserFixture.aUser()
        val userId = user.id
        val invalidTaskName = "a".repeat(256)
        val input = CreateTaskInputDTO(
            userId = userId.asString(),
            taskName = invalidTaskName
        )

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(DomainException::class.java)
        verify(exactly = 0) { taskRepository.save(any()) }
    }
}
