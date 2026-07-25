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

@Service
class CreateUserUseCaseImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CreateUserUseCase {
    override fun execute(input: CreateUserInputDTO): CreateUserOutputDTO {
        val encodedPassword = passwordEncoder.encode(input.password)
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            username = input.username,
            password = encodedPassword
        )

        if (userRepository.existsByUsername(user.username)) {
            throw UsernameAlreadyExistsException()
        }

        val saved = userRepository.save(user)

        return toOutput(saved)
    }

    private fun toOutput(user: UserEntity): CreateUserOutputDTO =
        CreateUserOutputDTO(
            id = user.id.asString(),
            username = user.username.asString()
        )
}
