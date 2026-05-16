package com.jpmns.task.core.presentation.controller.common.filter

import java.util.UUID

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import com.jpmns.task.core.presentation.controller.common.resolver.AuthenticatedUserResolver
import com.jpmns.task.shared.extension.addValue

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.baggage.BaggageBuilder

@Component
class TracingContextFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val correlationId = request.getHeader(HEADER_CORRELATION_ID) ?: UUID.randomUUID().toString()

        val baggageBuilder = Baggage.builder()

        if (!isApplicationRequest(request)) {
            populate(request, baggageBuilder, correlationId)
        }

        val scope = baggageBuilder.build().makeCurrent()

        try {
            filterChain.doFilter(request, response)
        } finally {
            scope.close()
            clear()
        }
    }

    private fun isApplicationRequest(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri.contains("swagger") || uri.contains("docs")
    }

    private fun populate(
        request: HttpServletRequest,
        baggageBuilder: BaggageBuilder,
        correlationId: String
    ) {
        baggageBuilder.addValue(MDC_CORRELATION_ID, correlationId)

        AuthenticatedUserResolver
            .getUserIdOrNull()
            ?.also { baggageBuilder.addValue(MDC_USER_ID, it) }

        request.requestURI?.also {
            baggageBuilder.addValue(MDC_PATH, it)
        }

        request.method?.also {
            baggageBuilder.addValue(MDC_METHOD, it)
        }
    }

    private fun clear() {
        MDC.remove(MDC_USER_ID)
        MDC.remove(MDC_CORRELATION_ID)
        MDC.remove(MDC_PATH)
        MDC.remove(MDC_METHOD)
    }

    companion object {
        private const val HEADER_CORRELATION_ID = "correlation-id"
        private const val MDC_USER_ID = "user-id"
        private const val MDC_CORRELATION_ID = "correlation-id"
        private const val MDC_PATH = "path"
        private const val MDC_METHOD = "method"
    }
}
