package com.jpmns.task.core.domain.user.valueobject

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource

import com.jpmns.task.shared.fixture.UserFixture

class UserPasswordValueObjectTest {
    @Test
    fun `should create a valid UserPasswordValueObject from a valid password`() {
        val user = UserFixture.aUser()
        val password = user.password

        val result = UserPasswordValueObject.of(password.asString())

        assertThat(result.isFail).isFalse()
        assertThat(result.getRealValue().asString()).isEqualTo(password.asString())
    }

    @Test
    fun `should accept a password with special characters`() {
        val user = UserFixture.aUser()
        val password = user.password
        val passwordWithSpecialChars = password.asString() + "!@#\$%^&*()"

        val result = UserPasswordValueObject.of(passwordWithSpecialChars)

        assertThat(result.isFail).isFalse()
    }

    @ParameterizedTest
    @EmptySource
    fun `should fail for empty password`(password: String) {
        val result = UserPasswordValueObject.of(password)

        assertThat(result.isFail).isTrue()
    }
}
