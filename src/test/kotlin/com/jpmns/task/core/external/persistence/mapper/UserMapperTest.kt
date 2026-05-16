package com.jpmns.task.core.external.persistence.mapper

import java.time.Instant
import java.util.UUID

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import com.jpmns.task.core.external.persistence.model.UserJpaModel
import com.jpmns.task.shared.fixture.UserFixture

class UserMapperTest {
    @Test
    fun `should map a UserEntity to a UserJpaModel correctly`() {
        val user = UserFixture.aUser()
        val expectedId = user.id.asString()
        val expectedUsername = user.username.asString()
        val expectedPassword = user.password.asString()

        val model = UserMapper.toModel(user)

        assertThat(model).isNotNull()
        assertThat(model.id.toString()).isEqualTo(expectedId)
        assertThat(model.username).isEqualTo(expectedUsername)
        assertThat(model.password).isEqualTo(expectedPassword)
        assertThat(model.createdAt).isNotNull()
    }

    @Test
    fun `should map a UserJpaModel to a UserEntity correctly`() {
        val user = UserFixture.aUser()
        val id = UUID.fromString(user.id.asString())
        val username = user.username.asString()
        val password = user.password.asString()
        val now = Instant.now()
        val model = UserJpaModel(
            id = id,
            username = username,
            password = password,
            createdAt = now,
            updatedAt = now
        )

        val entity = UserMapper.toDomain(model)

        assertThat(entity).isNotNull()
        assertThat(entity.id.asString()).isEqualTo(id.toString())
        assertThat(entity.username.asString()).isEqualTo(username)
        assertThat(entity.password.asString()).isEqualTo(password)
    }

    @Test
    fun `should preserve username when mapping from model to domain`() {
        val customUsername = "customuserpassword"
        val password = "other-password"
        val id = UUID.randomUUID()
        val now = Instant.now()
        val model = UserJpaModel(
            id = id,
            username = customUsername,
            password = password,
            createdAt = now,
            updatedAt = now
        )

        val entity = UserMapper.toDomain(model)

        assertThat(entity.username.asString()).isEqualTo(customUsername)
    }

    @Test
    fun `should preserve password when mapping from model to domain`() {
        val customPassword = "custom-password"
        val username = "otherusername"
        val id = UUID.randomUUID()
        val now = Instant.now()
        val model = UserJpaModel(
            id = id,
            username = username,
            password = customPassword,
            createdAt = now,
            updatedAt = now
        )

        val entity = UserMapper.toDomain(model)

        assertThat(entity.password.asString()).isEqualTo(customPassword)
    }

    @Test
    fun `should map createdAt from entity to model`() {
        val user = UserFixture.aUser()

        val model = UserMapper.toModel(user)

        assertThat(model.createdAt).isEqualTo(user.createdAt)
    }
}
