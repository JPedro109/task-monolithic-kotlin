package com.jpmns.task.shared.security.factory

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory

import com.jpmns.task.shared.security.WithJwtTokenMock

class WithMockJwtTokenSecurityContextFactory : WithSecurityContextFactory<WithJwtTokenMock> {
    override fun createSecurityContext(annotation: WithJwtTokenMock): SecurityContext {
        val httpStatus = annotation.httpStatus

        if (httpStatus == HttpStatus.UNAUTHORIZED) {
            return unauthorizedContext()
        }

        if (httpStatus == HttpStatus.FORBIDDEN) {
            return forbiddenContext(annotation.sub)
        }

        return authenticatedContext(annotation.sub)
    }

    private fun unauthorizedContext(): SecurityContext =
        SecurityContextHolder.createEmptyContext()

    private fun forbiddenContext(sub: String): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val authentication = UsernamePasswordAuthenticationToken(sub, null)
        context.authentication = authentication

        return context
    }

    private fun authenticatedContext(sub: String): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = UsernamePasswordAuthenticationToken(sub, null, authorities)
        context.authentication = authentication

        return context
    }
}
