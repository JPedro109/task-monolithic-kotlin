package com.jpmns.task.core.application.usecase.task

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository
import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDTO
import com.jpmns.task.core.application.usecase.task.implementation.CreateTaskUseCaseImpl
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
        val task = TaskFixture.aTask()
        val user = UserFixture.aUser()
        val userId = user.id
        val taskName = task.taskName
        val input = CreateTaskInputDTO(
            userId = userId.asString(),
            taskName = taskName.asString()
        )

        every { taskRepository.save(any()) } returns task

        val output = useCase.execute(input)

        assertThat(output.id).isEqualTo(task.id.asString())
        assertThat(output.userId).isEqualTo(task.userId.asString())
        assertThat(output.taskName).isEqualTo(taskName.asString())
        assertThat(output.finished).isFalse()
        assertThat(output.createdAt).isEqualTo(task.createdAt)

        verify { taskRepository.save(any()) }
    }
}
