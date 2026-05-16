package com.jpmns.task.core.external.persistence.mapper

import java.time.Instant
import java.util.UUID

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import com.jpmns.task.core.external.persistence.model.TaskJpaModel
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.fixture.UserFixture

@DisplayName("TaskMapper Tests")
class TaskMapperTest {
    @Test
    fun `should map a TaskEntity to a TaskJpaModel correctly`() {
        val task = TaskFixture.aTask()
        val expectedId = task.id.asString()
        val expectedUserId = task.userId.asString()
        val expectedTaskName = task.taskName.asString()

        val model = TaskMapper.toModel(task)

        assertThat(model).isNotNull()
        assertThat(model.id.toString()).isEqualTo(expectedId)
        assertThat(model.userId.toString()).isEqualTo(expectedUserId)
        assertThat(model.taskName).isEqualTo(expectedTaskName)
        assertThat(model.finished).isFalse()
        assertThat(model.createdAt).isNotNull()
    }

    @Test
    fun `should map a TaskJpaModel to a TaskEntity correctly`() {
        val task = TaskFixture.aTask()
        val user = UserFixture.aUser()
        val taskId = UUID.fromString(task.id.asString())
        val userId = UUID.fromString(user.id.asString())
        val taskName = task.taskName.asString()
        val finished = task.finished
        val now = Instant.now()
        val model = TaskJpaModel(
            id = taskId,
            userId = userId,
            taskName = taskName,
            finished = finished,
            createdAt = now,
            updatedAt = now
        )

        val entity = TaskMapper.toDomain(model)

        assertThat(entity).isNotNull()
        assertThat(entity.id.asString()).isEqualTo(taskId.toString())
        assertThat(entity.userId.asString()).isEqualTo(userId.toString())
        assertThat(entity.taskName.asString()).isEqualTo(taskName)
        assertThat(entity.finished).isFalse()
    }

    @Test
    fun `should preserve task name when mapping from model to domain`() {
        val task = TaskFixture.aTask()
        val user = UserFixture.aUser()
        val customName = "Custom task name"
        val taskId = UUID.fromString(task.id.asString())
        val userId = UUID.fromString(user.id.asString())
        val now = Instant.now()
        val model = TaskJpaModel(
            id = taskId,
            userId = userId,
            taskName = customName,
            finished = task.finished,
            createdAt = now,
            updatedAt = now
        )

        val entity = TaskMapper.toDomain(model)

        assertThat(entity.taskName.asString()).isEqualTo(customName)
    }

    @Test
    fun `should map finished state correctly from entity to model`() {
        val task = TaskFixture.aTask()
        task.markAsFinished()

        val model = TaskMapper.toModel(task)

        assertThat(model.finished).isTrue()
    }

    @Test
    fun `should map finished state correctly from model to domain`() {
        val task = TaskFixture.aTask()
        val user = UserFixture.aUser()
        val taskId = UUID.fromString(task.id.asString())
        val userId = UUID.fromString(user.id.asString())
        val taskName = task.taskName.asString()
        val now = Instant.now()
        val model = TaskJpaModel(
            id = taskId,
            userId = userId,
            taskName = taskName,
            finished = true,
            createdAt = now,
            updatedAt = now
        )

        val entity = TaskMapper.toDomain(model)

        assertThat(entity.finished).isTrue()
    }
}
