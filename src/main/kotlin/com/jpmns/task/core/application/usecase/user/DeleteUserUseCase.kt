package com.jpmns.task.core.application.usecase.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDto
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class DeleteUserUseCase(
    private val userRepository: UserRepository
) {
    @Transactional
    fun execute(input: DeleteUserInputDto) {
        val idResult = IdValueObject.of(input.userId).getValueResultOrThrow()

        userRepository.findById(idResult) ?: throw UserNotFoundException()

        userRepository.deleteById(idResult)
    }
}
