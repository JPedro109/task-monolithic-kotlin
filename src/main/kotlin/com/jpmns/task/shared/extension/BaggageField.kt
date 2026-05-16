package com.jpmns.task.shared.extension

import org.slf4j.MDC

import io.opentelemetry.api.baggage.BaggageBuilder

fun BaggageBuilder.addValue(key: String, value: String): BaggageBuilder {
    MDC.put(key, value)

    return this.put(key, value)
}
