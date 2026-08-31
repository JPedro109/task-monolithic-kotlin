package com.jpmns.task.core.application.usecase.user

import org.springframework.stereotype.Service

import com.jpmns.task.core.application.port.persistence.repository.UserRepository
import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO
import com.jpmns.task.core.application.usecase.user.dto.output.UserOutputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.user.UserEntity

@Service
class GetUserByIdUseCase(
    private val userRepository: UserRepository
) {
    fun execute(input: GetUserByIdInputDTO): UserOutputDTO {
        val idResult = IdValueObject.of(input.id).getValueResultOrThrow()

        val user = userRepository.findById(idResult) ?: throw UserNotFoundException()

        return toOutput(user)
    }

    private fun toOutput(user: UserEntity): UserOutputDTO =
        UserOutputDTO(
            id = user.id.asString(),
            username = user.username.asString(),
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
}
