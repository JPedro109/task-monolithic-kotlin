package com.jpmns.task.integration

import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.jpmns.task.integration.common.abstracts.IntegrationTestBase
import com.jpmns.task.integration.common.sql.SqlCreateSeed
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.security.WithJwtTokenMock

class TaskIntegrationTest : IntegrationTestBase() {
    @Nested
    @DisplayName("POST /api/v1/tasks")
    inner class CreateTask {
        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 201 with task data when input is valid`() {
            val task = TaskFixture.aTask()
            val taskName = task.taskName
            val taskNameString = taskName.asString()

            perform(taskNameString)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").isNotEmpty)
                .andExpect(jsonPath("$.taskName").value(taskNameString))
                .andExpect(jsonPath("$.finished").value(false))
                .andExpect(jsonPath("$.createdAt").isNotEmpty)
        }

        @Test
        fun `should return 401 when no token is provided`() {
            val task = TaskFixture.aTask()
            val taskName = task.taskName
            val taskNameString = taskName.asString()

            perform(taskNameString)
                .andExpect(status().isUnauthorized)
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 400 when taskName is blank`() {
            val emptyTaskName = ""

            perform(emptyTaskName)
                .andExpect(status().isBadRequest)
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 400 when taskName exceeds 255 characters`() {
            val longTaskName = "a".repeat(256)

            perform(longTaskName)
                .andExpect(status().isBadRequest)
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
    @DisplayName("GET /api/v1/tasks")
    inner class ListTasks {
        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 200 with all tasks belonging to the authenticated user`() {
            val task = TaskFixture.aTask()
            val taskName = task.taskName
            val taskNameString = taskName.asString()

            perform()
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(1)))
                .andExpect(jsonPath("$[0].taskName").value(taskNameString))
        }

        @Test
        fun `should return 401 when no token is provided`() {
            perform()
                .andExpect(status().isUnauthorized)
        }

        private fun perform(): ResultActions =
            mockMvc.perform(get("/api/v1/tasks"))
    }

    @Nested
    @DisplayName("PUT /api/v1/tasks/{id}")
    inner class UpdateTask {
        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 200 with updated task name when input is valid`() {
            val taskName = "Updated name"

            perform(EXISTING_TASK_ID, taskName)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(EXISTING_TASK_ID))
                .andExpect(jsonPath("$.taskName").value(taskName))
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock(sub = USER_ID_WITHOUT_TASK)
        fun `should return 403 when user does not own the task`() {
            val taskName = "Updated name"

            perform(EXISTING_TASK_ID, taskName)
                .andExpect(status().isForbidden)
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 404 when task does not exist`() {
            val taskName = "Updated name"

            perform(NON_EXISTENT_TASK_ID, taskName)
                .andExpect(status().isNotFound)
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 400 when taskName is blank`() {
            val emptyTaskName = ""

            perform(EXISTING_TASK_ID, emptyTaskName)
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 401 when no token is provided`() {
            val taskName = "Updated name"

            perform(EXISTING_TASK_ID, taskName)
                .andExpect(status().isUnauthorized)
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
    @DisplayName("DELETE /api/v1/tasks/{id}")
    inner class DeleteTask {
        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 204 when user owns the task`() {
            perform(EXISTING_TASK_ID)
                .andExpect(status().isNoContent)

            mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(jsonPath("$[?(@.id == '$EXISTING_TASK_ID')]").doesNotExist())
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock(sub = USER_ID_WITHOUT_TASK)
        fun `should return 403 when user does not own the task`() {
            perform(EXISTING_TASK_ID)
                .andExpect(status().isForbidden)
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 404 when task does not exist`() {
            perform(NON_EXISTENT_TASK_ID)
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 401 when no token is provided`() {
            perform(EXISTING_TASK_ID)
                .andExpect(status().isUnauthorized)
        }

        private fun perform(taskId: String): ResultActions =
            mockMvc.perform(delete("/api/v1/tasks/$taskId"))
    }

    @Nested
    @DisplayName("PATCH /api/v1/tasks/{id}/finish")
    inner class MarkTaskAsFinished {
        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 204 and task should be marked as finished`() {
            perform(EXISTING_TASK_ID)
                .andExpect(status().isNoContent)

            mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(jsonPath("$[?(@.id == '$EXISTING_TASK_ID')].finished").value(true))
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock(sub = USER_ID_WITHOUT_TASK)
        fun `should return 403 when user does not own the task`() {
            perform(EXISTING_TASK_ID)
                .andExpect(status().isForbidden)
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 404 when task does not exist`() {
            perform(NON_EXISTENT_TASK_ID)
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 401 when no token is provided`() {
            perform(EXISTING_TASK_ID)
                .andExpect(status().isUnauthorized)
        }

        private fun perform(taskId: String): ResultActions =
            mockMvc.perform(patch("/api/v1/tasks/$taskId/finish"))
    }

    private companion object {
        private const val EXISTING_TASK_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901"
        private const val NON_EXISTENT_TASK_ID = "00000000-0000-0000-0000-000000000001"
        private const val USER_ID_WITHOUT_TASK = "41a385a3-de9f-44bb-ac0f-7a9fd6ac11e1"
    }
}
