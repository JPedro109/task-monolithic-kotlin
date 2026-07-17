package com.jpmns.task.core.external.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import com.jpmns.task.core.application.port.security.Token
import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO
import com.jpmns.task.core.application.usecase.user.interfaces.GetUserByIdUseCase

@Component
class JwtAuthenticationFilter(
    private val token: Token,
    private val getUserByIdUseCase: GetUserByIdUseCase
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val jwt = authHeader.substring(BEARER_PREFIX_LENGTH)

            val decoded = runCatching { token.tokenValidation(jwt) }.getOrNull()

            if (decoded == null) {
                return
            }

            val input = GetUserByIdInputDTO(id = decoded.sub)
            val user = getUserByIdUseCase.execute(input = input)

            val authentication = UsernamePasswordAuthenticationToken(
                user.id,
                null,
                emptyList()
            )

            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        private const val BEARER_PREFIX_LENGTH = 7
    }
}
