package com.jpmns.task.core.presentation.controller

import jakarta.validation.Valid

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.jpmns.task.core.application.usecase.user.dto.input.RefreshUserTokenInputDTO
import com.jpmns.task.core.application.usecase.user.dto.input.UserLoginInputDTO
import com.jpmns.task.core.application.usecase.user.interfaces.RefreshUserTokenUseCase
import com.jpmns.task.core.application.usecase.user.interfaces.UserLoginUseCase
import com.jpmns.task.core.presentation.controller.documentation.AuthControllerDoc
import com.jpmns.task.core.presentation.controller.payload.user.request.RefreshTokenRequest
import com.jpmns.task.core.presentation.controller.payload.user.request.UserLoginRequest
import com.jpmns.task.core.presentation.controller.payload.user.response.RefreshTokenResponse
import com.jpmns.task.core.presentation.controller.payload.user.response.UserLoginResponse

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userLoginUseCase: UserLoginUseCase,
    private val refreshUserTokenUseCase: RefreshUserTokenUseCase,
) : AuthControllerDoc {
    @PostMapping("/login")
    override fun login(@Valid @RequestBody request: UserLoginRequest): ResponseEntity<UserLoginResponse> {
        logger.info("Login request received for user: ${request.username}")

        val input = UserLoginInputDTO(username = request.username, password = request.password)

        val output = userLoginUseCase.execute(input)

        val response = UserLoginResponse.of(output)

        logger.info("Login successful for user: ${request.username}")
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    override fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> {
        logger.info("Refresh token request received")

        val input = RefreshUserTokenInputDTO(refreshToken = request.refreshToken)

        val output = refreshUserTokenUseCase.execute(input)

        val response = RefreshTokenResponse.of(output)

        logger.info("Token refreshed successfully")
        return ResponseEntity.ok(response)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthController::class.java)
    }
}
