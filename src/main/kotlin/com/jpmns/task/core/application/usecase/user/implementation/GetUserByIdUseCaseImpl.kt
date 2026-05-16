package com.jpmns.task.core.application.usecase.user.implementation

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.UserOutputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.interfaces.GetUserByIdUseCase
import com.jpmns.task.core.domain.common.valueobject.IdValueObject

@Service
class GetUserByIdUseCaseImpl(
    private val userRepository: UserRepository
) : GetUserByIdUseCase {
    override fun execute(input: GetUserByIdInputDTO): UserOutputDTO {
        val idResult = IdValueObject.of(input.id)
        if (idResult.isFail) {
            throw idResult.getFailureError()
        }

        val id = idResult.getSuccessValue()

        val user = userRepository.findById(id) ?: throw UserNotFoundException()

        return UserOutputDTO(
            id = user.id.asString(),
            username = user.username.asString(),
            password = user.password.asString(),
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}
