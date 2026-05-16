package com.jpmns.task.core.external.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class PasswordEncoderAdapterTest {
    private lateinit var passwordEncoderAdapter: PasswordEncoderAdapter

    @BeforeEach
    fun setUp() {
        passwordEncoderAdapter = PasswordEncoderAdapter(BCryptPasswordEncoder())
    }

    @Test
    fun `should encode a raw password and return a non-null hash`() {
        val raw = RAW_PASSWORD

        val encoded = passwordEncoderAdapter.encode(raw)

        assertThat(encoded).isNotNull()
        assertThat(encoded).isNotEqualTo(raw)
    }

    @Test
    fun `should produce different hashes for the same password on each call`() {
        val raw = RAW_PASSWORD

        val first = passwordEncoderAdapter.encode(raw)
        val second = passwordEncoderAdapter.encode(raw)

        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun `should return true when raw password matches the encoded one`() {
        val raw = RAW_PASSWORD
        val encoded = passwordEncoderAdapter.encode(raw)

        val result = passwordEncoderAdapter.matches(raw, encoded)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when raw password does not match the encoded one`() {
        val wrong = "wrong-password"
        val encoded = passwordEncoderAdapter.encode(RAW_PASSWORD)

        val result = passwordEncoderAdapter.matches(wrong, encoded)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when comparing against an empty string`() {
        val emptyRawPassword = ""
        val encoded = passwordEncoderAdapter.encode(RAW_PASSWORD)

        val result = passwordEncoderAdapter.matches(emptyRawPassword, encoded)

        assertThat(result).isFalse()
    }

    private companion object {
        const val RAW_PASSWORD = "raw-password"
    }
}
