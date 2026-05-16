package com.jpmns.task.core.domain.user.valueobject

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.ValueSource

import com.jpmns.task.shared.fixture.UserFixture

class UsernameValueObjectTest {
    @Test
    fun `should create a valid UsernameValueObject from a valid username`() {
        val user = UserFixture.aUser()
        val username = user.username

        val result = UsernameValueObject.of(username.asString())

        assertThat(result.isFail).isFalse()
        assertThat(result.getSuccessValue().asString()).isEqualTo(username.asString())
    }

    @Test
    fun `should accept a username with exactly 3 characters`() {
        val username = "abc"

        val result = UsernameValueObject.of(username)

        assertThat(result.isFail).isFalse()
    }

    @Test
    fun `should accept a username with exactly 50 characters`() {
        val username = "a".repeat(50)

        val result = UsernameValueObject.of(username)

        assertThat(result.isFail).isFalse()
    }

    @Test
    fun `should fail for a username with more than 50 characters`() {
        val username = "a".repeat(51)

        val result = UsernameValueObject.of(username)

        assertThat(result.isFail).isTrue()
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(
        strings = [
            "ab",
            "user name",
            "user@name",
            "user-name",
            "user.name"
        ]
    )
    fun `should fail for empty or invalid usernames`(username: String) {
        val result = UsernameValueObject.of(username)

        assertThat(result.isFail).isTrue()
    }
}
