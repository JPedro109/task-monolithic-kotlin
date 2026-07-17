package com.jpmns.task.core.presentation.controller

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.jpmns.task.configuration.security.SecurityConfig
import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.port.security.exception.InvalidTokenException
import com.jpmns.task.core.application.usecase.user.dto.output.RefreshUserTokenOutputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.UserLoginOutputDTO
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException
import com.jpmns.task.core.application.usecase.user.implementation.GetUserByIdUseCaseImpl
import com.jpmns.task.core.application.usecase.user.interfaces.RefreshUserTokenUseCase
import com.jpmns.task.core.application.usecase.user.interfaces.UserLoginUseCase
import com.jpmns.task.core.presentation.controller.common.handler.GlobalExceptionHandler
import com.jpmns.task.shared.fixture.UserFixture
import com.ninjasquad.springmockk.MockkBean

import io.mockk.every

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var userLoginUseCase: UserLoginUseCase

    @MockkBean
    private lateinit var refreshUserTokenUseCase: RefreshUserTokenUseCase

    @MockkBean
    private lateinit var token: Token

    @MockkBean
    private lateinit var getUserByIdUseCaseImpl: GetUserByIdUseCaseImpl

    @Nested
    inner class Login {
        @Test
        fun `should return 200 with tokens when login succeeds`() {
            val user = UserFixture.aUser()
            val username = user.username
            val password = user.password
            val accessToken = "access-token"
            val refreshToken = "refresh-token"
            val output = UserLoginOutputDTO(accessToken, refreshToken)

            every { userLoginUseCase.execute(any()) } returns output

            val result = perform(username.asString(), password.asString())

            result.andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken))
        }

        @Test
        fun `should return 401 when credentials are invalid`() {
            val user = UserFixture.aUser()
            val username = user.username
            val wrongPassword = "wrong-password"

            every { userLoginUseCase.execute(any()) } throws InvalidCredentialsException()

            val result = perform(username.asString(), wrongPassword)

            result.andExpect(status().isUnauthorized)
        }

        @Test
        fun `should return 400 when username is blank`() {
            val emptyUsername = ""
            val password = "some-password"

            val result = perform(emptyUsername, password)

            result.andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 400 when password is blank`() {
            val user = UserFixture.aUser()
            val username = user.username
            val emptyPassword = ""

            val result = perform(username.asString(), emptyPassword)

            result.andExpect(status().isBadRequest)
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
    inner class Refresh {
        @Test
        fun `should return 200 with new tokens when refresh succeeds`() {
            val refreshTokenValue = "valid-refresh-token"
            val newAccessToken = "new-access-token"
            val newRefreshToken = "new-refresh-token"
            val output = RefreshUserTokenOutputDTO(newAccessToken, newRefreshToken)

            every { refreshUserTokenUseCase.execute(any()) } returns output

            val result = perform(refreshTokenValue)

            result.andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").value(newRefreshToken))
        }

        @Test
        fun `should return 401 when refresh token is invalid`() {
            val invalidRefreshToken = "invalid-refresh-token"

            every { refreshUserTokenUseCase.execute(any()) } throws InvalidTokenException()

            val result = perform(invalidRefreshToken)

            result.andExpect(status().isUnauthorized)
        }

        @Test
        fun `should return 400 when refresh token is blank`() {
            val emptyRefreshToken = ""

            val result = perform(emptyRefreshToken)

            result.andExpect(status().isBadRequest)
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
