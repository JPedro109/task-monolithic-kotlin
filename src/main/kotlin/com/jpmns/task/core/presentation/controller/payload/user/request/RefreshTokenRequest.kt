package com.jpmns.task.core.presentation.controller.payload.user.request

import jakarta.validation.constraints.NotBlank

import com.jpmns.task.core.presentation.controller.documentation.payload.user.request.RefreshTokenRequestDoc

data class RefreshTokenRequest(
    @field:NotBlank
    override val refreshToken: String,
) : RefreshTokenRequestDoc
