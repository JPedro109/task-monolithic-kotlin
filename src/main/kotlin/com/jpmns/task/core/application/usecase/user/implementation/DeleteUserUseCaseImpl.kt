package com.jpmns.task.core.application.usecase.user.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.interfaces.DeleteUserUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class DeleteUserUseCaseImpl(
    private val userRepository: UserRepository,
) : DeleteUserUseCase {
    override fun execute(input: DeleteUserInputDTO) {
        val idResult = IdValueObject.of(input.userId)
        if (idResult.isFail) {
            throw idResult.getRealError()
        }

        val id = idResult.getRealValue()

        userRepository.findById(id) ?: throw UserNotFoundException()

        userRepository.deleteById(id)
    }
}
