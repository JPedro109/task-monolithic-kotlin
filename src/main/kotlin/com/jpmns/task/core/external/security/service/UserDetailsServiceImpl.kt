package com.jpmns.task.core.external.security.service

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO
import com.jpmns.task.core.application.usecase.user.interfaces.GetUserByIdUseCase

@Service
class UserDetailsServiceImpl(
    private val getUserByIdUseCase: GetUserByIdUseCase
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val userOutput = getUserByIdUseCase.execute(GetUserByIdInputDTO(id = username))

        return User(userOutput.id, userOutput.password, emptyList())
    }
}
