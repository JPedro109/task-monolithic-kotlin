package com.jpmns.task.core.application.usecase.user.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUsernameInputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.UpdateUsernameOutputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUsernameUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.user.UserEntity
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject

@Service
class UpdateUsernameUseCaseImpl(
    private val userRepository: UserRepository
) : UpdateUsernameUseCase {
    override fun execute(input: UpdateUsernameInputDTO): UpdateUsernameOutputDTO {
        val idResult = IdValueObject.of(input.userId).getValueResultOrThrow()
        val newUsernameResult = UsernameValueObject.of(input.newUsername).getValueResultOrThrow()

        val user = userRepository.findById(idResult) ?: throw UserNotFoundException()

        if (userRepository.existsByUsername(newUsernameResult)) {
            throw UsernameAlreadyExistsException()
        }

        user.updateUsername(input.newUsername)

        val saved = userRepository.save(user)

        return toOutput(saved)
    }

    private fun toOutput(user: UserEntity): UpdateUsernameOutputDTO =
        UpdateUsernameOutputDTO(
            id = user.id.asString(),
            username = user.username.asString()
        )
}
