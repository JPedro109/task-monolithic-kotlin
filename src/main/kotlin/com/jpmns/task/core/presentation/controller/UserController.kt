package com.jpmns.task.core.presentation.controller

import jakarta.validation.Valid

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.jpmns.task.core.application.usecase.user.dto.input.CreateUserInputDTO
import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDTO
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUserPasswordInputDTO
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUsernameInputDTO
import com.jpmns.task.core.application.usecase.user.interfaces.CreateUserUseCase
import com.jpmns.task.core.application.usecase.user.interfaces.DeleteUserUseCase
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUserPasswordUseCase
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUsernameUseCase
import com.jpmns.task.core.presentation.controller.common.resolver.AuthenticatedUserResolver
import com.jpmns.task.core.presentation.controller.documentation.UserControllerDoc
import com.jpmns.task.core.presentation.controller.payload.user.request.CreateUserRequest
import com.jpmns.task.core.presentation.controller.payload.user.request.UpdateUserPasswordRequest
import com.jpmns.task.core.presentation.controller.payload.user.request.UpdateUsernameRequest
import com.jpmns.task.core.presentation.controller.payload.user.response.CreateUserResponse
import com.jpmns.task.core.presentation.controller.payload.user.response.UpdateUsernameResponse

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val createUserUseCase: CreateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val updateUserPasswordUseCase: UpdateUserPasswordUseCase,
    private val updateUsernameUseCase: UpdateUsernameUseCase,
) : UserControllerDoc {
    @PostMapping
    override fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<CreateUserResponse> {
        logger.info("Create user request received: $request")

        val input = CreateUserInputDTO(username = request.username, password = request.password)

        val output = createUserUseCase.execute(input)

        val response = CreateUserResponse.of(output)

        logger.info("User created with id: ${response.id}")
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping
    override fun deleteUser(): ResponseEntity<Void> {
        logger.info("Delete user request received")

        val userId = AuthenticatedUserResolver.getUserId()

        val input = DeleteUserInputDTO(userId = userId)

        deleteUserUseCase.execute(input)

        logger.info("User deleted: $userId")
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/password")
    override fun updatePassword(
        @Valid @RequestBody request: UpdateUserPasswordRequest,
    ): ResponseEntity<Void> {
        logger.info("Update password request received: $request")

        val userId = AuthenticatedUserResolver.getUserId()

        val input = UpdateUserPasswordInputDTO(
            userId = userId,
            currentPassword = request.currentPassword,
            newPassword = request.newPassword
        )

        updateUserPasswordUseCase.execute(input)

        logger.info("Password updated for user: $userId")
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/username")
    override fun updateUsername(
        @Valid @RequestBody request: UpdateUsernameRequest,
    ): ResponseEntity<UpdateUsernameResponse> {
        logger.info("Update username request received")

        val userId = AuthenticatedUserResolver.getUserId()

        val input = UpdateUsernameInputDTO(userId = userId, newUsername = request.newUsername)

        val output = updateUsernameUseCase.execute(input)

        val response = UpdateUsernameResponse.of(output)

        logger.info("Username updated for user: $userId")
        return ResponseEntity.ok(response)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }
}
