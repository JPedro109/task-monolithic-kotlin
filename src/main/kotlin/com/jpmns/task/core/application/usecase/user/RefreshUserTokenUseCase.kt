package com.jpmns.task.core.application.usecase.user

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.usecase.user.dto.input.RefreshUserTokenInputDto
import com.jpmns.task.core.application.usecase.user.dto.output.RefreshUserTokenOutputDto
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class RefreshUserTokenUseCase(
    private val userRepository: UserRepository,
    private val token: Token
) {
    fun execute(input: RefreshUserTokenInputDto): RefreshUserTokenOutputDto {
        val decoded = token.tokenValidation(input.refreshToken)

        val idResult = IdValueObject.of(decoded.sub).getValueResultOrThrow()

        userRepository.findById(idResult) ?: throw UserNotFoundException()

        val accessToken = token.generateAccessToken(decoded.sub)
        val refreshToken = token.generateRefreshToken(decoded.sub)

        return RefreshUserTokenOutputDto(accessToken = accessToken, refreshToken = refreshToken)
    }
}
