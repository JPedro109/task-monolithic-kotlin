package com.jpmns.task.configuration.tracing

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator

@Configuration
@ConditionalOnProperty(name = ["management.tracing.enabled"], havingValue = "true")
class OtelBaggageConfig {
    @Bean
    fun contextPropagators(): ContextPropagators {
        val propagator = TextMapPropagator.composite(
            W3CTraceContextPropagator.getInstance(),
            W3CBaggagePropagator.getInstance()
        )
        return ContextPropagators.create(propagator)
    }
}
