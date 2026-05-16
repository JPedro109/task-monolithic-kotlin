package com.jpmns.task.core.application.usecase.user.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.usecase.user.dto.input.RefreshUserTokenInputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.RefreshUserTokenOutputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.interfaces.RefreshUserTokenUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class RefreshUserTokenUseCaseImpl(
    private val userRepository: UserRepository,
    private val token: Token
) : RefreshUserTokenUseCase {
    override fun execute(input: RefreshUserTokenInputDTO): RefreshUserTokenOutputDTO {
        val decoded = token.tokenValidation(input.refreshToken)

        val idResult = IdValueObject.of(decoded.sub)
        if (idResult.isFail) {
            throw idResult.getFailureError()
        }

        val id = idResult.getSuccessValue()

        userRepository.findById(id) ?: throw UserNotFoundException()

        val accessToken = token.generateAccessToken(decoded.sub)
        val refreshToken = token.generateRefreshToken(decoded.sub)

        return RefreshUserTokenOutputDTO(accessToken = accessToken, refreshToken = refreshToken)
    }
}
