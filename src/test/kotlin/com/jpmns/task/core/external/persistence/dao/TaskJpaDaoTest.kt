package com.jpmns.task.core.external.persistence.dao

import java.util.UUID

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException

import com.jpmns.task.core.external.persistence.model.TaskJpaModel
import com.jpmns.task.core.external.persistence.model.UserJpaModel
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.fixture.UserFixture

@DataJpaTest
class TaskJpaDaoTest {
    @Autowired
    private lateinit var taskJpaDao: TaskJpaDao

    @Autowired
    private lateinit var userJpaDao: UserJpaDao

    private lateinit var userId: UUID

    @BeforeEach
    fun setUp() {
        val user = UserFixture.aUser()
        val model = buildUser(user)
        userJpaDao.save(model)
        userId = model.id
    }

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

    private fun buildTask(
        task: com.jpmns.task.core.domain.task.TaskEntity,
        userId: UUID
    ): TaskJpaModel {
        val id = UUID.fromString(task.id.asString())
        val taskName = task.taskName.asString()
        val finished = task.finished
        val createdAt = task.createdAt

        return TaskJpaModel(
            id = id,
            userId = userId,
            taskName = taskName,
            finished = finished,
            createdAt = createdAt,
            updatedAt = null
        )
    }

    @Test
    fun `should save a task and return it with populated timestamps`() {
        val task = TaskFixture.aTask()
        val model = buildTask(task, userId)
        val expectedTaskName = task.taskName.asString()

        val saved = taskJpaDao.save(model)

        assertThat(saved).isNotNull()
        assertThat(saved.id).isEqualTo(model.id)
        assertThat(saved.userId).isEqualTo(userId)
        assertThat(saved.taskName).isEqualTo(expectedTaskName)
        assertThat(saved.finished).isFalse()
        assertThat(saved.createdAt).isNotNull()
    }

    @Test
    fun `should find a task by id after saving`() {
        val task = TaskFixture.aTask()
        val model = buildTask(task, userId)
        val expectedTaskName = task.taskName.asString()
        val taskId = model.id
        taskJpaDao.save(model)

        val found = taskJpaDao.findById(taskId)

        assertThat(found).isPresent()
        assertThat(found.get().id).isEqualTo(model.id)
        assertThat(found.get().taskName).isEqualTo(expectedTaskName)
    }

    @Test
    fun `should return empty Optional when task id does not exist`() {
        val nonExistentId = UUID.randomUUID()

        val found = taskJpaDao.findById(nonExistentId)

        assertThat(found).isEmpty()
    }

    @Test
    fun `should return empty list when user has no tasks`() {
        val otherUserId = UUID.randomUUID()

        val tasks = taskJpaDao.findAllByUserId(otherUserId)

        assertThat(tasks).isEmpty()
    }

    @Test
    fun `should delete a task by id`() {
        val task = TaskFixture.aTask()
        val model = buildTask(task, userId)
        val taskId = model.id
        taskJpaDao.save(model)

        taskJpaDao.deleteById(taskId)

        val found = taskJpaDao.findById(taskId)
        assertThat(found).isEmpty()
    }

    @Test
    fun `should update task name when saving an existing task`() {
        val task = TaskFixture.aTask()
        val model = buildTask(task, userId)
        val taskId = model.id
        val updatedName = "Updated name"
        taskJpaDao.save(model)
        model.taskName = updatedName

        taskJpaDao.save(model)

        val found = taskJpaDao.findById(taskId)
        assertThat(found).isPresent()
        assertThat(found.get().taskName).isEqualTo(updatedName)
    }

    @Test
    fun `should mark a task as finished when updating`() {
        val task = TaskFixture.aTask()
        val model = buildTask(task, userId)
        val taskId = model.id
        taskJpaDao.save(model)
        model.finished = true

        taskJpaDao.save(model)

        val found = taskJpaDao.findById(taskId)
        assertThat(found).isPresent()
        assertThat(found.get().finished).isTrue()
    }

    @Test
    fun `should throw when saving a task with a non-existent user_id`() {
        val task = TaskFixture.aTask()
        val nonExistentUserId = UUID.randomUUID()
        val orphanModel = buildTask(task, nonExistentUserId)

        assertThatThrownBy {
            taskJpaDao.save(orphanModel)
            taskJpaDao.flush()
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `should cascade delete tasks when the owner user is deleted`() {
        val task = TaskFixture.aTask()
        val model = buildTask(task, userId)
        taskJpaDao.save(model)
        taskJpaDao.flush()

        userJpaDao.deleteById(userId)
        userJpaDao.flush()

        val tasks = taskJpaDao.findAllByUserId(userId)
        assertThat(tasks).isEmpty()
    }

    @Test
    fun `should find all saved tasks`() {
        val task = TaskFixture.aTask()
        val model = buildTask(task, userId)
        taskJpaDao.save(model)

        val all = taskJpaDao.findAll()

        assertThat(all).hasSizeGreaterThanOrEqualTo(1)
    }
}
