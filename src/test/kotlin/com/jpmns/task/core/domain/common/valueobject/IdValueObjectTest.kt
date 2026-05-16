package com.jpmns.task.core.domain.common.valueobject

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.ValueSource

import com.jpmns.task.shared.fixture.UserFixture

class IdValueObjectTest {
    @Test
    fun `should create a valid IdValueObject from a UUID string`() {
        val user = UserFixture.aUser()
        val id = user.id

        val result = IdValueObject.of(id.asString())

        assertThat(result.isFail).isFalse()
        assertThat(result.getRealValue().asString()).isEqualTo(id.asString())
    }

    @Test
    fun `should accept an uppercase UUID string`() {
        val user = UserFixture.aUser()
        val id = user.id
        val uppercaseId = id.asString().uppercase()

        val result = IdValueObject.of(uppercaseId)

        assertThat(result.isFail).isFalse()
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(
        strings = [
            "not-a-uuid",
            "12345678-1234-1234-1234-12345678901",
            "12345678-1234-1234-1234-1234567890123",
            "12345678_1234_1234_1234_123456789012",
            "gggggggg-gggg-gggg-gggg-gggggggggggg"
        ]
    )
    fun `should fail for empty or malformed UUID strings`(id: String) {
        val result = IdValueObject.of(id)

        assertThat(result.isFail).isTrue()
    }
}
