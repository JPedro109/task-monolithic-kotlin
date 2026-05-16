package com.jpmns.task.core.external.security.service

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException
import com.jpmns.task.core.application.usecase.user.interfaces.GetUserByIdUseCase
import com.jpmns.task.core.domain.common.exception.DomainException

@Service
class UserDetailsServiceImpl(
    private val getUserByIdUseCase: GetUserByIdUseCase,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val userOutput = try {
            getUserByIdUseCase.execute(GetUserByIdInputDTO(id = username))
        } catch (ex: UserNotFoundException) {
            throw UsernameNotFoundException("User not found: $username", ex)
        } catch (ex: DomainException) {
            throw UsernameNotFoundException("User not found: $username", ex)
        }
        return User(userOutput.id, userOutput.password, emptyList())
    }
}
