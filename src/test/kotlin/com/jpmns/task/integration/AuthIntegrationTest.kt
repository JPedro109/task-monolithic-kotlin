package com.jpmns.task.integration

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.fasterxml.jackson.databind.ObjectMapper
import com.jpmns.task.integration.common.abstracts.IntegrationTestBase
import com.jpmns.task.integration.common.sql.SqlCreateSeed

@DisplayName("Auth Integration Tests")
class AuthIntegrationTest : IntegrationTestBase() {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    inner class Login {
        @Test
        @SqlCreateSeed
        fun `should return 200 with access and refresh tokens when credentials are valid`() {
            val username = "john"
            val password = "password"

            perform(username, password)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").isNotEmpty)
                .andExpect(jsonPath("$.refreshToken").isNotEmpty)
        }

        @Test
        fun `should return 401 when password is wrong`() {
            val wrongUsername = "wrongusername"
            val wrongPassword = "wrong-password"

            perform(wrongUsername, wrongPassword)
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `should return 401 when user does not exist`() {
            val nonExistenceUsername = "nonexistenceuser"
            val password = "password"

            perform(nonExistenceUsername, password)
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `should return 400 when username is blank`() {
            val emptyUsername = ""
            val password = "password"

            perform(emptyUsername, password)
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 400 when password is blank`() {
            val username = "john"
            val emptyPassword = ""

            perform(username, emptyPassword)
                .andExpect(status().isBadRequest)
        }

        private fun perform(username: String, password: String): ResultActions {
            val requestBody = """{"username": "$username", "password": "$password"}"""

            return mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    inner class Refresh {
        @Test
        @SqlCreateSeed
        fun `should return 200 with new access token when refresh token is valid`() {
            val loginBody = """{"username": "john", "password": "password"}"""
            val response = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody)
            )
                .andExpect(status().isOk)
                .andReturn()
            val json = objectMapper.readTree(response.response.contentAsString)
            val refreshToken = json.get("refreshToken").asText()

            perform(refreshToken)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").isNotEmpty)
        }

        @Test
        fun `should return 401 when refresh token is invalid`() {
            val invalidToken = "invalid-token"

            perform(invalidToken)
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `should return 400 when refresh token is blank`() {
            val emptyToken = ""

            perform(emptyToken)
                .andExpect(status().isBadRequest)
        }

        private fun perform(refreshToken: String): ResultActions {
            val requestBody = """{"refreshToken": "$refreshToken"}"""

            return mockMvc.perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
        }
    }
}
