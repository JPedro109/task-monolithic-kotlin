package com.jpmns.task.core.external.persistence.mapper

import java.util.UUID

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import com.jpmns.task.core.external.persistence.model.TaskJpaModel
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.fixture.UserFixture

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
        val taskIdVO = task.id
        val userIdVO = user.id
        val taskName = task.taskName.asString()
        val finished = task.finished
        val createdAt = task.createdAt
        val taskId = UUID.fromString(taskIdVO.asString())
        val userId = UUID.fromString(userIdVO.asString())
        val model = TaskJpaModel(
            id = taskId,
            userId = userId,
            taskName = taskName,
            finished = finished,
            createdAt = createdAt,
            updatedAt = null
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
        val taskIdVO = task.id
        val userIdVO = user.id
        val finished = task.finished
        val createdAt = task.createdAt
        val taskId = UUID.fromString(taskIdVO.asString())
        val userId = UUID.fromString(userIdVO.asString())
        val model = TaskJpaModel(
            id = taskId,
            userId = userId,
            taskName = customName,
            finished = finished,
            createdAt = createdAt,
            updatedAt = null
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
        val taskIdVO = task.id
        val userIdVO = user.id
        val taskName = task.taskName.asString()
        val createdAt = task.createdAt
        val taskId = UUID.fromString(taskIdVO.asString())
        val userId = UUID.fromString(userIdVO.asString())
        val model = TaskJpaModel(
            id = taskId,
            userId = userId,
            taskName = taskName,
            finished = true,
            createdAt = createdAt,
            updatedAt = null
        )

        val entity = TaskMapper.toDomain(model)

        assertThat(entity.finished).isTrue()
    }
}
