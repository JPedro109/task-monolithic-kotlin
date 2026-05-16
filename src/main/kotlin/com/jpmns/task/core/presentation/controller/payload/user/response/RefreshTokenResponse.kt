package com.jpmns.task.core.presentation.controller.payload.user.response

import com.jpmns.task.core.application.usecase.user.dto.output.RefreshUserTokenOutputDTO
import com.jpmns.task.core.presentation.controller.documentation.payload.user.response.RefreshTokenResponseDoc

data class RefreshTokenResponse(
    override val accessToken: String,
    override val refreshToken: String,
) : RefreshTokenResponseDoc {
    companion object {
        fun of(dto: RefreshUserTokenOutputDTO): RefreshTokenResponse =
            RefreshTokenResponse(
                accessToken = dto.accessToken,
                refreshToken = dto.refreshToken
            )
    }
}
