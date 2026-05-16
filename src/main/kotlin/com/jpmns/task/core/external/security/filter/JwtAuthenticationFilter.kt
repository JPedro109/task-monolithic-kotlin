package com.jpmns.task.core.external.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import com.jpmns.task.core.application.port.security.Token

@Component
class JwtAuthenticationFilter(
    private val token: Token,
    private val userDetailsService: UserDetailsService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val jwt = authHeader.substring(BEARER_PREFIX_LENGTH)

            val decoded = runCatching { token.tokenValidation(jwt) }.getOrNull()

            if (decoded == null) {
                return
            }

            val userDetails = userDetailsService.loadUserByUsername(decoded.sub)

            val authentication = UsernamePasswordAuthenticationToken(
                userDetails.username,
                null,
                userDetails.authorities,
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
