package com.jpmns.task.core.application.usecase.user.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.port.security.PasswordEncoder
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUserPasswordInputDTO
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUserPasswordUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class UpdateUserPasswordUseCaseImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : UpdateUserPasswordUseCase {
    override fun execute(input: UpdateUserPasswordInputDTO) {
        val idResult = IdValueObject.of(input.userId)
        if (idResult.isFail) {
            throw idResult.getFailureError()
        }

        val id = idResult.getSuccessValue()

        val user = userRepository.findById(id) ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(input.currentPassword, user.password.asString())) {
            throw InvalidCredentialsException()
        }

        val encodedNewPassword = passwordEncoder.encode(input.newPassword)
        user.updatePassword(encodedNewPassword)
        userRepository.save(user)
    }
}
