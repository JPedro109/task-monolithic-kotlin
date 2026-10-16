package com.jpmns.task.core.presentation.controller.payload.user.response

import com.jpmns.task.core.application.usecase.user.dto.output.UserLoginOutputDto
import com.jpmns.task.core.presentation.controller.documentation.payload.user.response.UserLoginResponseDoc

data class UserLoginResponse(
    override val accessToken: String,
    override val refreshToken: String
) : UserLoginResponseDoc {
    override fun toString(): String =
        "UserLoginResponse{accessToken='[PROTECTED]', refreshToken='[PROTECTED]'}"

    companion object {
        fun of(dto: UserLoginOutputDto): UserLoginResponse =
            UserLoginResponse(
                accessToken = dto.accessToken,
                refreshToken = dto.refreshToken
            )
    }
}
