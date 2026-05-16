package com.jpmns.task.core.external.persistence.repository

import java.time.Instant
import java.util.Optional
import java.util.UUID

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject
import com.jpmns.task.core.external.persistence.dao.UserJpaDao
import com.jpmns.task.core.external.persistence.model.UserJpaModel
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class UserRepositoryAdapterTest {
    @MockK
    private lateinit var dao: UserJpaDao

    @InjectMockKs
    private lateinit var adapter: UserRepositoryAdapter

    private fun buildUserModel(): UserJpaModel {
        val user = UserFixture.aUser()
        val id = UUID.fromString(user.id.asString())
        val username = user.username.asString()
        val password = user.password.asString()
        val now = Instant.now()

        return UserJpaModel(
            id = id,
            username = username,
            password = password,
            createdAt = now,
            updatedAt = now
        )
    }

    @Test
    fun `should save a user and return the persisted domain entity`() {
        val user = UserFixture.aUser()
        val expectedUserId = user.id.asString()
        val expectedUsername = user.username.asString()
        val model = buildUserModel()

        every { dao.save(any()) } returns model

        val result = adapter.save(user)

        assertThat(result).isNotNull()
        assertThat(result.id.asString()).isEqualTo(expectedUserId)
        assertThat(result.username.asString()).isEqualTo(expectedUsername)
        verify { dao.save(any()) }
    }

    @Test
    fun `should find a user by id and return the domain entity`() {
        val user = UserFixture.aUser()
        val userIdVO = user.id
        val id = IdValueObject.of(userIdVO.asString()).getRealValue()
        val formattedId = UUID.fromString(userIdVO.asString())
        val model = buildUserModel()

        every { dao.findById(formattedId) } returns Optional.of(model)

        val result = adapter.findById(id)

        assertThat(result).isNotNull()
        assertThat(result!!.id.asString()).isEqualTo(userIdVO.asString())
    }

    @Test
    fun `should return null when user is not found by id`() {
        val user = UserFixture.aUser()
        val userIdVO = user.id
        val id = IdValueObject.of(userIdVO.asString()).getRealValue()

        every { dao.findById(any()) } returns Optional.empty()

        val result = adapter.findById(id)

        assertThat(result).isNull()
    }

    @Test
    fun `should find a user by username and return the domain entity`() {
        val user = UserFixture.aUser()
        val usernameVO = user.username
        val username = UsernameValueObject.of(usernameVO.asString()).getRealValue()
        val model = buildUserModel()

        every { dao.findByUsername(usernameVO.asString()) } returns model

        val result = adapter.findByUsername(username)

        assertThat(result).isNotNull()
        assertThat(result!!.username.asString()).isEqualTo(usernameVO.asString())
    }

    @Test
    fun `should return null when user is not found by username`() {
        val user = UserFixture.aUser()
        val usernameVO = user.username
        val username = UsernameValueObject.of(usernameVO.asString()).getRealValue()

        every { dao.findByUsername(any()) } returns null

        val result = adapter.findByUsername(username)

        assertThat(result).isNull()
    }

    @Test
    fun `should return true when username exists`() {
        val user = UserFixture.aUser()
        val usernameVO = user.username
        val username = UsernameValueObject.of(usernameVO.asString()).getRealValue()

        every { dao.existsByUsername(usernameVO.asString()) } returns true

        val result = adapter.existsByUsername(username)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when username does not exist`() {
        val user = UserFixture.aUser()
        val usernameVO = user.username
        val username = UsernameValueObject.of(usernameVO.asString()).getRealValue()

        every { dao.existsByUsername(any()) } returns false

        val result = adapter.existsByUsername(username)

        assertThat(result).isFalse()
    }

    @Test
    fun `should delete a user by id`() {
        val user = UserFixture.aUser()
        val userIdVO = user.id
        val id = IdValueObject.of(userIdVO.asString()).getRealValue()
        val formattedId = UUID.fromString(userIdVO.asString())

        every { dao.deleteById(any()) } returns Unit

        adapter.deleteById(id)

        verify { dao.deleteById(formattedId) }
    }
}
