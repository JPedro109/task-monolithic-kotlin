package com.jpmns.task.core.application.usecase.user.implementation

import java.util.UUID

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.port.security.PasswordEncoder
import com.jpmns.task.core.application.usecase.user.dto.input.CreateUserInputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.CreateUserOutputDTO
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException
import com.jpmns.task.core.application.usecase.user.interfaces.CreateUserUseCase
import com.jpmns.task.core.domain.user.UserEntity
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject

@Service
class CreateUserUseCaseImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : CreateUserUseCase {
    override fun execute(input: CreateUserInputDTO): CreateUserOutputDTO {
        val usernameResult = UsernameValueObject.of(input.username)
        if (usernameResult.isFail) {
            throw usernameResult.getRealError()
        }

        val username = usernameResult.getRealValue()

        if (userRepository.existsByUsername(username)) {
            throw UsernameAlreadyExistsException()
        }

        val encodedPassword = passwordEncoder.encode(input.password)

        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            username = input.username,
            password = encodedPassword,
        )
        val saved = userRepository.save(user)

        return CreateUserOutputDTO(
            id = saved.id.asString(),
            username = saved.username.asString()
        )
    }
}
