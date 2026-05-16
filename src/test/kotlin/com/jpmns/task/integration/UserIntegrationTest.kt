package com.jpmns.task.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.jpmns.task.integration.common.abstracts.IntegrationTestBase
import com.jpmns.task.integration.common.sql.SqlCreateSeed
import com.jpmns.task.shared.security.WithJwtTokenMock

class UserIntegrationTest : IntegrationTestBase() {
    companion object {
        private const val EXISTING_USERNAME = "john"
        private const val PASSWORD = "password"
    }

    @Nested
    inner class CreateUser {
        @Test
        fun `should return 201 with id and username when input is valid`() {
            val username = "username"

            perform(username, PASSWORD)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").isNotEmpty)
                .andExpect(jsonPath("$.username").value(username))
        }

        @Test
        @SqlCreateSeed
        fun `should return 409 when username already exists`() {
            perform(EXISTING_USERNAME, PASSWORD)
                .andExpect(status().isConflict)
        }

        @Test
        fun `should return 400 when username is too short`() {
            val username = "ab"

            perform(username, PASSWORD)
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 400 when password is too short`() {
            val username = "username"
            val password = "ab"

            perform(username, password)
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 400 when username is blank`() {
            val username = ""
            val password = "ab"

            perform(username, password)
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 400 when password is blank`() {
            val username = "username"
            val password = ""

            perform(username, password)
                .andExpect(status().isBadRequest)
        }

        private fun perform(username: String, password: String): ResultActions {
            val requestBody = """{"username": "$username", "password": "$password"}"""

            return mockMvc.perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
        }
    }

    @Nested
    inner class DeleteUser {
        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 204 when user is authenticated`() {
            perform()
                .andExpect(status().isNoContent)
        }

        @Test
        fun `should return 401 when no token is provided`() {
            perform()
                .andExpect(status().isUnauthorized)
        }

        private fun perform(): ResultActions =
            mockMvc.perform(delete("/api/v1/users"))
    }

    @Nested
    inner class UpdateUsername {
        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 200 with new username when input is valid`() {
            val newUsername = "newusername"

            perform(newUsername)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.username").value(newUsername))
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 409 when new username is already taken`() {
            perform(EXISTING_USERNAME)
                .andExpect(status().isConflict)
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 400 when new username is too short`() {
            val newUsername = "ab"

            perform(newUsername)
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 401 when no token is provided`() {
            val newUsername = "newusername"

            perform(newUsername)
                .andExpect(status().isUnauthorized)
        }

        private fun perform(newUsername: String): ResultActions {
            val requestBody = """{"newUsername": "$newUsername"}"""

            return mockMvc.perform(
                patch("/api/v1/users/username")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
        }
    }

    @Nested
    inner class UpdatePassword {
        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 204 when current password is correct`() {
            val newPassword = "new-password"

            perform(PASSWORD, newPassword)
                .andExpect(status().isNoContent)
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 401 when current password is wrong`() {
            val currentPassword = "wrong-password"
            val newPassword = "new-password"

            perform(currentPassword, newPassword)
                .andExpect(status().isUnauthorized)
        }

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 400 when new password is too short`() {
            val newPassword = "ab"

            perform(PASSWORD, newPassword)
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 401 when no token is provided`() {
            val newPassword = "new-password"

            perform(PASSWORD, newPassword)
                .andExpect(status().isUnauthorized)
        }

        private fun perform(currentPassword: String, newPassword: String): ResultActions {
            val requestBody = """{"currentPassword": "$currentPassword", "newPassword": "$newPassword"}"""

            return mockMvc.perform(
                patch("/api/v1/users/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
        }
    }
}
