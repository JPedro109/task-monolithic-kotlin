package com.jpmns.task.core.domain.user

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.UserFixture

class UserEntityTest {
    @Test
    fun `should create a user with valid data`() {
        val user = UserFixture.aUser()
        val id = user.id
        val username = user.username
        val password = user.password

        val newUser = UserEntity(
            id = id.asString(),
            username = username.asString(),
            password = password.asString()
        )

        assertThat(newUser.id.asString()).isEqualTo(id.asString())
        assertThat(newUser.username.asString()).isEqualTo(username.asString())
        assertThat(newUser.password.asString()).isEqualTo(password.asString())
        assertThat(newUser.createdAt).isNotNull()
    }

    @Test
    fun `should update the username`() {
        val user = UserFixture.aUser()
        val newUsername = "newusername"

        user.updateUsername(newUsername)

        assertThat(user.username.asString()).isEqualTo(newUsername)
    }

    @Test
    fun `should update the password`() {
        val user = UserFixture.aUser()
        val newPassword = "new-password"

        user.updatePassword(newPassword)

        assertThat(user.password.asString()).isEqualTo(newPassword)
    }

    @Test
    fun `should throw when username is too short`() {
        val user = UserFixture.aUser()
        val id = user.id
        val password = user.password
        val invalidUsername = "ab"

        assertThatThrownBy {
            UserEntity(
                id = id.asString(),
                username = invalidUsername,
                password = password.asString()
            )
        }.isInstanceOf(DomainException::class.java)
    }

    @Test
    fun `should throw when password is empty`() {
        val user = UserFixture.aUser()
        val id = user.id
        val username = user.username
        val emptyPassword = ""

        assertThatThrownBy {
            UserEntity(
                id = id.asString(),
                username = username.asString(),
                password = emptyPassword
            )
        }.isInstanceOf(DomainException::class.java)
    }

    @Test
    fun `should throw with two errors when both username and password are invalid`() {
        val user = UserFixture.aUser()
        val id = user.id
        val invalidUsername = "ab"
        val emptyPassword = ""

        assertThatThrownBy {
            UserEntity(
                id = id.asString(),
                username = invalidUsername,
                password = emptyPassword
            )
        }.isInstanceOf(DomainException::class.java)
            .satisfies({ ex ->
                val errors = (ex as DomainException).errors
                assertThat(errors).hasSize(2)
            })
    }

    @Test
    fun `should throw when id is not a valid UUID`() {
        val user = UserFixture.aUser()
        val username = user.username
        val password = user.password
        val invalidId = "not-a-uuid"

        assertThatThrownBy {
            UserEntity(
                id = invalidId,
                username = username.asString(),
                password = password.asString()
            )
        }.isInstanceOf(DomainException::class.java)
    }

    @Test
    fun `should throw when updating with an invalid username`() {
        val user = UserFixture.aUser()
        val invalidUsername = "ab"

        assertThatThrownBy {
            user.updateUsername(invalidUsername)
        }.isInstanceOf(DomainException::class.java)
    }

    @Test
    fun `should throw when updating with an empty password`() {
        val user = UserFixture.aUser()
        val emptyPassword = ""

        assertThatThrownBy {
            user.updatePassword(emptyPassword)
        }.isInstanceOf(DomainException::class.java)
    }
}
