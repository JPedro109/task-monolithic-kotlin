package com.jpmns.task.core.presentation.controller

import java.time.Instant

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.jpmns.task.configuration.security.SecurityConfig
import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException
import com.jpmns.task.core.application.usecase.task.interfaces.CreateTaskUseCase
import com.jpmns.task.core.application.usecase.task.interfaces.DeleteTaskUseCase
import com.jpmns.task.core.application.usecase.task.interfaces.ListTasksUseCase
import com.jpmns.task.core.application.usecase.task.interfaces.MarkTaskAsFinishedUseCase
import com.jpmns.task.core.application.usecase.task.interfaces.UpdateTaskUseCase
import com.jpmns.task.core.presentation.controller.common.handler.GlobalExceptionHandler
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.fixture.UserFixture
import com.jpmns.task.shared.security.WithJwtTokenMock
import com.ninjasquad.springmockk.MockkBean

import io.mockk.every

@WebMvcTest(TaskController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
class TaskControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var createTaskUseCase: CreateTaskUseCase

    @MockkBean
    private lateinit var listTasksUseCase: ListTasksUseCase

    @MockkBean
    private lateinit var updateTaskUseCase: UpdateTaskUseCase

    @MockkBean
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase

    @MockkBean
    private lateinit var markTaskAsFinishedUseCase: MarkTaskAsFinishedUseCase

    @MockkBean
    private lateinit var token: Token

    @MockkBean
    private lateinit var userDetailsService: UserDetailsService

    private fun buildTaskOutput(): TaskOutputDTO {
        val task = TaskFixture.aTask()
        val user = UserFixture.aUser()

        return TaskOutputDTO(
            id = task.id.asString(),
            userId = user.id.asString(),
            taskName = task.taskName.asString(),
            finished = task.finished,
            createdAt = Instant.now()
        )
    }

    @Nested
    inner class CreateTask {
        @Test
        @WithJwtTokenMock
        fun `should return 201 with task data when creation succeeds`() {
            val task = TaskFixture.aTask()
            val taskName = task.taskName
            val output = buildTaskOutput()

            every { createTaskUseCase.execute(any()) } returns output

            val result = perform(taskName.asString())

            result.andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(output.id))
                .andExpect(jsonPath("$.taskName").value(taskName.asString()))
        }

        @Test
        fun `should return 401 when request has no token`() {
            val task = TaskFixture.aTask()
            val taskName = task.taskName

            val result = perform(taskName.asString())

            result.andExpect(status().isUnauthorized)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 400 when task name is blank`() {
            val emptyTaskName = ""

            val result = perform(emptyTaskName)

            result.andExpect(status().isBadRequest)
        }

        private fun perform(taskName: String): ResultActions {
            val requestBody = """{"taskName": "$taskName"}"""

            return mockMvc.perform(
                post("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
        }
    }

    @Nested
    inner class ListTasks {
        @Test
        @WithJwtTokenMock
        fun `should return 200 with list of tasks`() {
            val output = buildTaskOutput()

            every { listTasksUseCase.execute(any()) } returns listOf(output)

            val result = perform()

            result.andExpect(status().isOk)
                .andExpect(jsonPath("$[0].id").value(output.id))
                .andExpect(jsonPath("$[0].taskName").value(output.taskName))
        }

        @Test
        @WithJwtTokenMock
        fun `should return 200 with empty list when user has no tasks`() {
            every { listTasksUseCase.execute(any()) } returns emptyList()

            val result = perform()

            result.andExpect(status().isOk)
                .andExpect(jsonPath("$").isEmpty)
        }

        @Test
        fun `should return 401 when request has no token`() {
            val result = perform()

            result.andExpect(status().isUnauthorized)
        }

        private fun perform(): ResultActions =
            mockMvc.perform(get("/api/v1/tasks"))
    }

    @Nested
    inner class UpdateTask {
        @Test
        @WithJwtTokenMock
        fun `should return 200 with updated task when update succeeds`() {
            val task = TaskFixture.aTask()
            val taskId = task.id
            val updatedName = "Updated task name"
            val output = buildTaskOutput()

            every { updateTaskUseCase.execute(any()) } returns output

            val result = perform(taskId.asString(), updatedName)

            result.andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(output.id))
        }

        @Test
        @WithJwtTokenMock
        fun `should return 404 when task is not found`() {
            val task = TaskFixture.aTask()
            val taskId = task.id
            val taskName = task.taskName

            every { updateTaskUseCase.execute(any()) } throws TaskNotFoundException()

            val result = perform(taskId.asString(), taskName.asString())

            result.andExpect(status().isNotFound)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 403 when user does not own the task`() {
            val task = TaskFixture.aTask()
            val taskId = task.id
            val taskName = task.taskName

            every { updateTaskUseCase.execute(any()) } throws TaskAccessDeniedException()

            val result = perform(taskId.asString(), taskName.asString())

            result.andExpect(status().isForbidden)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 400 when task name is blank`() {
            val task = TaskFixture.aTask()
            val taskId = task.id
            val emptyTaskName = ""

            val result = perform(taskId.asString(), emptyTaskName)

            result.andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 401 when request has no token`() {
            val task = TaskFixture.aTask()
            val taskId = task.id
            val taskName = task.taskName

            val result = perform(taskId.asString(), taskName.asString())

            result.andExpect(status().isUnauthorized)
        }

        private fun perform(taskId: String, taskName: String): ResultActions {
            val requestBody = """{"taskName": "$taskName"}"""

            return mockMvc.perform(
                put("/api/v1/tasks/$taskId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
        }
    }

    @Nested
    inner class DeleteTask {
        @Test
        @WithJwtTokenMock
        fun `should return 204 when task is deleted successfully`() {
            val task = TaskFixture.aTask()
            val taskId = task.id

            every { deleteTaskUseCase.execute(any()) } returns Unit

            val result = perform(taskId.asString())

            result.andExpect(status().isNoContent)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 404 when task is not found`() {
            val task = TaskFixture.aTask()
            val taskId = task.id

            every { deleteTaskUseCase.execute(any()) } throws TaskNotFoundException()

            val result = perform(taskId.asString())

            result.andExpect(status().isNotFound)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 403 when user does not own the task`() {
            val task = TaskFixture.aTask()
            val taskId = task.id

            every { deleteTaskUseCase.execute(any()) } throws TaskAccessDeniedException()

            val result = perform(taskId.asString())

            result.andExpect(status().isForbidden)
        }

        @Test
        fun `should return 401 when request has no token`() {
            val task = TaskFixture.aTask()
            val taskId = task.id

            val result = perform(taskId.asString())

            result.andExpect(status().isUnauthorized)
        }

        private fun perform(taskId: String): ResultActions =
            mockMvc.perform(delete("/api/v1/tasks/$taskId"))
    }

    @Nested
    inner class MarkAsFinished {
        @Test
        @WithJwtTokenMock
        fun `should return 204 when task is marked as finished`() {
            val task = TaskFixture.aTask()
            val taskId = task.id

            every { markTaskAsFinishedUseCase.execute(any()) } returns Unit

            val result = perform(taskId.asString())

            result.andExpect(status().isNoContent)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 404 when task is not found`() {
            val task = TaskFixture.aTask()
            val taskId = task.id

            every { markTaskAsFinishedUseCase.execute(any()) } throws TaskNotFoundException()

            val result = perform(taskId.asString())

            result.andExpect(status().isNotFound)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 403 when user does not own the task`() {
            val task = TaskFixture.aTask()
            val taskId = task.id

            every { markTaskAsFinishedUseCase.execute(any()) } throws TaskAccessDeniedException()

            val result = perform(taskId.asString())

            result.andExpect(status().isForbidden)
        }

        @Test
        fun `should return 401 when request has no token`() {
            val task = TaskFixture.aTask()
            val taskId = task.id

            val result = perform(taskId.asString())

            result.andExpect(status().isUnauthorized)
        }

        private fun perform(taskId: String): ResultActions =
            mockMvc.perform(patch("/api/v1/tasks/$taskId/finish"))
    }
}
