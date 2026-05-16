package com.jpmns.task.core.presentation.controller.common.resolver

import org.springframework.security.core.context.SecurityContextHolder

object AuthenticatedUserResolver {
    fun getUserId(): String {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw IllegalArgumentException("User is not authenticated")

        return auth.principal?.toString()
            ?: throw IllegalArgumentException("Principal is null")
    }

    fun getUserIdOrNull(): String? =
        SecurityContextHolder.getContext().authentication?.principal?.toString()
}
