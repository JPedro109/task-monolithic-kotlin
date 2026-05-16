package com.jpmns.task.core.application.usecase.user.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUsernameInputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.UpdateUsernameOutputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUsernameUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject

@Service
class UpdateUsernameUseCaseImpl(
    private val userRepository: UserRepository,
) : UpdateUsernameUseCase {
    override fun execute(input: UpdateUsernameInputDTO): UpdateUsernameOutputDTO {
        val idResult = IdValueObject.of(input.userId)
        if (idResult.isFail) {
            throw idResult.getFailureError()
        }

        val id = idResult.getSuccessValue()

        val user = userRepository.findById(id) ?: throw UserNotFoundException()

        val newUsernameResult = UsernameValueObject.of(input.newUsername)
        if (newUsernameResult.isFail) {
            throw newUsernameResult.getFailureError()
        }
        val newUsername = newUsernameResult.getSuccessValue()

        if (userRepository.existsByUsername(newUsername)) {
            throw UsernameAlreadyExistsException()
        }

        user.updateUsername(input.newUsername)
        val saved = userRepository.save(user)

        return UpdateUsernameOutputDTO(
            id = saved.id.asString(),
            username = saved.username.asString()
        )
    }
}
