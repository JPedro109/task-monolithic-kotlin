package com.jpmns.task.core.application.usecase.user

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class DeleteUserUseCase(
    private val userRepository: UserRepository
) {
    fun execute(input: DeleteUserInputDTO) {
        val idResult = IdValueObject.of(input.userId).getValueResultOrThrow()

        userRepository.findById(idResult) ?: throw UserNotFoundException()

        userRepository.deleteById(idResult)
    }
}
