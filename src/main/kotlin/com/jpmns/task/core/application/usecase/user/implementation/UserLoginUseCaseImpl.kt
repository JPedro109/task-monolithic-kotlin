package com.jpmns.task.core.application.usecase.user.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.port.security.PasswordEncoder
import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.usecase.user.dto.input.UserLoginInputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.UserLoginOutputDTO
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException
import com.jpmns.task.core.application.usecase.user.interfaces.UserLoginUseCase
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject

@Service
class UserLoginUseCaseImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val token: Token
) : UserLoginUseCase {
    override fun execute(input: UserLoginInputDTO): UserLoginOutputDTO {
        val usernameResult = UsernameValueObject.of(input.username)
        if (usernameResult.isFail) {
            throw usernameResult.getFailureError()
        }

        val username = usernameResult.getSuccessValue()

        val user = userRepository.findByUsername(username) ?: throw InvalidCredentialsException()

        val passwordIsValid = passwordEncoder.matches(
            rawPassword = input.password,
            encodedPassword = user.password.asString()
        )
        if (!passwordIsValid) {
            throw InvalidCredentialsException()
        }

        val accessToken = token.generateAccessToken(user.id.asString())
        val refreshToken = token.generateRefreshToken(user.id.asString())

        return UserLoginOutputDTO(accessToken = accessToken, refreshToken = refreshToken)
    }
}
