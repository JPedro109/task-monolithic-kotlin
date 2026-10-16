package com.jpmns.task.core.presentation.controller.common.resolver

import org.springframework.security.core.context.SecurityContextHolder

import com.jpmns.task.core.presentation.controller.common.resolver.exception.UnauthenticatedException

object AuthenticatedUserResolver {
    fun getUserId(): String {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw UnauthenticatedException()

        return auth.principal?.toString()
            ?: throw UnauthenticatedException()
    }

    fun getUserIdOrNull(): String? =
        SecurityContextHolder.getContext().authentication?.principal?.toString()
}
