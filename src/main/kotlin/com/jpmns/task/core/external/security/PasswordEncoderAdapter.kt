package com.jpmns.task.core.external.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

import com.jpmns.task.core.application.port.security.PasswordEncoder

@Component
class PasswordEncoderAdapter(
    private val bCryptPasswordEncoder: BCryptPasswordEncoder
) : PasswordEncoder {
    override fun encode(rawPassword: String): String =
        bCryptPasswordEncoder.encode(rawPassword).toString()

    override fun matches(rawPassword: String, encodedPassword: String): Boolean =
        bCryptPasswordEncoder.matches(rawPassword, encodedPassword)
}
