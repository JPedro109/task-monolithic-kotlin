package com.jpmns.task.core.presentation.controller

import org.junit.jupiter.api.DisplayName
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.jpmns.task.configuration.security.SecurityConfig
import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.usecase.user.dto.output.CreateUserOutputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.UpdateUsernameOutputDTO
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException
import com.jpmns.task.core.application.usecase.user.interfaces.CreateUserUseCase
import com.jpmns.task.core.application.usecase.user.interfaces.DeleteUserUseCase
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUserPasswordUseCase
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUsernameUseCase
import com.jpmns.task.core.presentation.controller.common.handler.GlobalExceptionHandler
import com.jpmns.task.shared.fixture.UserFixture
import com.jpmns.task.shared.security.WithJwtTokenMock
import com.ninjasquad.springmockk.MockkBean

import io.mockk.every

@WebMvcTest(UserController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
@DisplayName("UserController Tests")
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var createUserUseCase: CreateUserUseCase

    @MockkBean
    private lateinit var deleteUserUseCase: DeleteUserUseCase

    @MockkBean
    private lateinit var updateUserPasswordUseCase: UpdateUserPasswordUseCase

    @MockkBean
    private lateinit var updateUsernameUseCase: UpdateUsernameUseCase

    @MockkBean
    private lateinit var token: Token

    @MockkBean
    private lateinit var userDetailsService: UserDetailsService

    @Nested
    @DisplayName("POST /api/v1/users")
    inner class CreateUser {
        @Test
        fun `should return 201 with user data when creation succeeds`() {
            val user = UserFixture.aUser()
            val userId = user.id
            val username = user.username
            val password = user.password
            val output = CreateUserOutputDTO(userId.asString(), username.asString())

            every { createUserUseCase.execute(any()) } returns output

            val result = perform(username.asString(), password.asString())

            result.andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(userId.asString()))
                .andExpect(jsonPath("$.username").value(username.asString()))
        }

        @Test
        fun `should return 409 when username already exists`() {
            val user = UserFixture.aUser()
            val username = user.username
            val password = user.password

            every { createUserUseCase.execute(any()) } throws UsernameAlreadyExistsException()

            val result = perform(username.asString(), password.asString())

            result.andExpect(status().isConflict)
        }

        @Test
        fun `should return 400 when username is blank`() {
            val user = UserFixture.aUser()
            val password = user.password
            val emptyUsername = ""

            val result = perform(emptyUsername, password.asString())

            result.andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 400 when username is shorter than 3 characters`() {
            val user = UserFixture.aUser()
            val password = user.password
            val shortUsername = "ab"

            val result = perform(shortUsername, password.asString())

            result.andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 400 when password is shorter than 8 characters`() {
            val user = UserFixture.aUser()
            val username = user.username
            val shortPassword = "ab"

            val result = perform(username.asString(), shortPassword)

            result.andExpect(status().isBadRequest)
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
    @DisplayName("DELETE /api/v1/users")
    inner class DeleteUser {
        @Test
        @WithJwtTokenMock
        fun `should return 204 when user is deleted successfully`() {
            every { deleteUserUseCase.execute(any()) } returns Unit

            val result = perform()

            result.andExpect(status().isNoContent)
        }

        @Test
        fun `should return 401 when request has no token`() {
            val result = perform()

            result.andExpect(status().isUnauthorized)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 404 when user is not found`() {
            every { deleteUserUseCase.execute(any()) } throws UserNotFoundException()

            val result = perform()

            result.andExpect(status().isNotFound)
        }

        private fun perform(): ResultActions =
            mockMvc.perform(delete("/api/v1/users"))
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/password")
    inner class UpdatePassword {
        @Test
        @WithJwtTokenMock
        fun `should return 204 when password is updated successfully`() {
            val oldPassword = "old-password"
            val newPassword = "new-password"

            every { updateUserPasswordUseCase.execute(any()) } returns Unit

            val result = perform(oldPassword, newPassword)

            result.andExpect(status().isNoContent)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 401 when current password is wrong`() {
            val wrongOldPassword = "wrong-pass"
            val newPassword = "new-password"

            every { updateUserPasswordUseCase.execute(any()) } throws InvalidCredentialsException()

            val result = perform(wrongOldPassword, newPassword)

            result.andExpect(status().isUnauthorized)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 400 when new password is shorter than 8 characters`() {
            val oldPassword = "old-password"
            val newPassword = "ab"

            val result = perform(oldPassword, newPassword)

            result.andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 401 when request has no token`() {
            val oldPassword = "old-password"
            val newPassword = "new-password"

            val result = perform(oldPassword, newPassword)

            result.andExpect(status().isUnauthorized)
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

    @Nested
    @DisplayName("PATCH /api/v1/users/username")
    inner class UpdateUsername {
        @Test
        @WithJwtTokenMock
        fun `should return 200 with updated username when update succeeds`() {
            val user = UserFixture.aUser()
            val userId = user.id
            val username = user.username
            val output = UpdateUsernameOutputDTO(userId.asString(), username.asString())

            every { updateUsernameUseCase.execute(any()) } returns output

            val result = perform(username.asString())

            result.andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(userId.asString()))
                .andExpect(jsonPath("$.username").value(username.asString()))
        }

        @Test
        @WithJwtTokenMock
        fun `should return 409 when new username already exists`() {
            val user = UserFixture.aUser()
            val username = user.username

            every { updateUsernameUseCase.execute(any()) } throws UsernameAlreadyExistsException()

            val result = perform(username.asString())

            result.andExpect(status().isConflict)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 400 when new username is shorter than 3 characters`() {
            val shortNewUsername = "ab"

            val result = perform(shortNewUsername)

            result.andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 401 when request has no token`() {
            val user = UserFixture.aUser()
            val username = user.username

            val result = perform(username.asString())

            result.andExpect(status().isUnauthorized)
        }

        @Test
        @WithJwtTokenMock
        fun `should return 404 when user is not found`() {
            val user = UserFixture.aUser()
            val username = user.username

            every { updateUsernameUseCase.execute(any()) } throws UserNotFoundException()

            val result = perform(username.asString())

            result.andExpect(status().isNotFound)
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
}
