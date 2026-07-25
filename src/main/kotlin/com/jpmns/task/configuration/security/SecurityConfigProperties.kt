package com.jpmns.task.configuration.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security")
data class SecurityConfigProperties(
    val jwt: Jwt
) {
    data class Jwt(
        val secret: String,
        val accessTokenExpirationMs: Long,
        val refreshTokenExpirationMs: Long
    )
}
