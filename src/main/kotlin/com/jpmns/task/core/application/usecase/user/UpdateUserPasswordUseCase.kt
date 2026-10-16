package com.jpmns.task.core.application.usecase.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.port.security.PasswordEncoder
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUserPasswordInputDto
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class UpdateUserPasswordUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun execute(input: UpdateUserPasswordInputDto) {
        val idResult = IdValueObject.of(input.userId).getValueResultOrThrow()

        val user = userRepository.findById(idResult) ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(input.currentPassword, user.password.asString())) {
            throw InvalidCredentialsException()
        }

        val encodedNewPassword = passwordEncoder.encode(input.newPassword)
        user.updatePassword(encodedNewPassword)

        userRepository.save(user)
    }
}
