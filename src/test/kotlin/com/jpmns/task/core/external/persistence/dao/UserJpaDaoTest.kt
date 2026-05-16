package com.jpmns.task.core.external.persistence.dao

import java.util.UUID

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException

import com.jpmns.task.core.external.persistence.model.UserJpaModel
import com.jpmns.task.shared.fixture.UserFixture

@DataJpaTest
class UserJpaDaoTest {
    @Autowired
    private lateinit var userJpaDao: UserJpaDao

    private fun buildUser(user: com.jpmns.task.core.domain.user.UserEntity): UserJpaModel {
        val id = UUID.fromString(user.id.asString())
        val username = user.username.asString()
        val password = user.password.asString()
        val createdAt = user.createdAt

        return UserJpaModel(
            id = id,
            username = username,
            password = password,
            createdAt = createdAt,
            updatedAt = null
        )
    }

    @Test
    fun `should save a user and return it with populated timestamps`() {
        val user = UserFixture.aUser()
        val model = buildUser(user)
        val expectedUsername = user.username.asString()
        val expectedPassword = user.password.asString()

        val saved = userJpaDao.save(model)

        assertThat(saved).isNotNull()
        assertThat(saved.id).isEqualTo(model.id)
        assertThat(saved.username).isEqualTo(expectedUsername)
        assertThat(saved.password).isEqualTo(expectedPassword)
        assertThat(saved.createdAt).isNotNull()
    }

    @Test
    fun `should find a user by id after saving`() {
        val user = UserFixture.aUser()
        val model = buildUser(user)
        val expectedUsername = user.username.asString()
        val userId = model.id
        userJpaDao.save(model)

        val found = userJpaDao.findById(userId)

        assertThat(found).isPresent()
        assertThat(found.get().id).isEqualTo(model.id)
        assertThat(found.get().username).isEqualTo(expectedUsername)
    }

    @Test
    fun `should return empty Optional when user id does not exist`() {
        val nonExistentId = UUID.randomUUID()

        val found = userJpaDao.findById(nonExistentId)

        assertThat(found).isEmpty()
    }

    @Test
    fun `should find a user by username`() {
        val user = UserFixture.aUser()
        val model = buildUser(user)
        val username = user.username.asString()
        userJpaDao.save(model)

        val found = userJpaDao.findByUsername(username)

        assertThat(found).isNotNull()
        assertThat(found!!.username).isEqualTo(username)
    }

    @Test
    fun `should return null when username does not exist`() {
        val nonExistentUsername = "nonexistentuser"

        val found = userJpaDao.findByUsername(nonExistentUsername)

        assertThat(found).isNull()
    }

    @Test
    fun `should return true when username exists`() {
        val user = UserFixture.aUser()
        val model = buildUser(user)
        val username = user.username.asString()
        userJpaDao.save(model)

        val exists = userJpaDao.existsByUsername(username)

        assertThat(exists).isTrue()
    }

    @Test
    fun `should return false when username does not exist`() {
        val nonExistentUsername = "nonexistentuser"

        val exists = userJpaDao.existsByUsername(nonExistentUsername)

        assertThat(exists).isFalse()
    }

    @Test
    fun `should delete a user by id`() {
        val user = UserFixture.aUser()
        val model = buildUser(user)
        val userId = model.id
        userJpaDao.save(model)

        userJpaDao.deleteById(userId)

        val found = userJpaDao.findById(userId)
        assertThat(found).isEmpty()
    }

    @Test
    fun `should update username when saving an existing user`() {
        val user = UserFixture.aUser()
        val model = buildUser(user)
        val userId = model.id
        val updatedUsername = "updateusername"
        userJpaDao.save(model)
        model.username = updatedUsername

        userJpaDao.save(model)

        val found = userJpaDao.findById(userId)
        assertThat(found).isPresent()
        assertThat(found.get().username).isEqualTo(updatedUsername)
    }

    @Test
    fun `should throw when saving a user with duplicate username`() {
        val user = UserFixture.aUser()
        val model = buildUser(user)
        val duplicateId = UUID.randomUUID()
        val duplicateUsername = user.username.asString()
        val duplicatePassword = user.password.asString()
        val duplicateModel = UserJpaModel(
            id = duplicateId,
            username = duplicateUsername,
            password = duplicatePassword,
            createdAt = null,
            updatedAt = null
        )
        userJpaDao.save(model)
        userJpaDao.flush()

        assertThatThrownBy {
            userJpaDao.save(duplicateModel)
            userJpaDao.flush()
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `should find all saved users`() {
        val user = UserFixture.aUser()
        val model = buildUser(user)
        userJpaDao.save(model)

        val all = userJpaDao.findAll()

        assertThat(all).hasSizeGreaterThanOrEqualTo(1)
    }
}
