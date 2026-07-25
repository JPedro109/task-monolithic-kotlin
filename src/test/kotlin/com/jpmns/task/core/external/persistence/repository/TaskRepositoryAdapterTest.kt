package com.jpmns.task.core.external.persistence.repository

import java.util.Optional
import java.util.UUID

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import com.jpmns.task.core.domain.common.valueobject.IdValueObject
import com.jpmns.task.core.external.persistence.dao.TaskJpaDao
import com.jpmns.task.core.external.persistence.model.TaskJpaModel
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.fixture.UserFixture

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify

@ExtendWith(MockKExtension::class)
class TaskRepositoryAdapterTest {
    @MockK
    private lateinit var dao: TaskJpaDao

    @InjectMockKs
    private lateinit var adapter: TaskRepositoryAdapter

    @Test
    fun `should save a task and return the persisted domain entity`() {
        val task = TaskFixture.aTask()
        val expectedTaskId = task.id.asString()
        val expectedTaskName = task.taskName.asString()
        val model = buildTaskModel()

        every { dao.save(any()) } returns model

        val result = adapter.save(task)

        assertThat(result).isNotNull()
        assertThat(result.id.asString()).isEqualTo(expectedTaskId)
        assertThat(result.taskName.asString()).isEqualTo(expectedTaskName)
        verify { dao.save(any()) }
    }

    @Test
    fun `should find a task by id and return the domain entity`() {
        val task = TaskFixture.aTask()
        val taskIdVO = task.id
        val id = IdValueObject.of(taskIdVO.asString()).getValueResult()
        val formattedId = UUID.fromString(taskIdVO.asString())
        val model = buildTaskModel()

        every { dao.findById(formattedId) } returns Optional.of(model)

        val result = adapter.findById(id)

        assertThat(result).isNotNull()
        assertThat(result!!.id.asString()).isEqualTo(taskIdVO.asString())
    }

    @Test
    fun `should return null when task is not found by id`() {
        val task = TaskFixture.aTask()
        val taskIdVO = task.id
        val id = IdValueObject.of(taskIdVO.asString()).getValueResult()

        every { dao.findById(any()) } returns Optional.empty()

        val result = adapter.findById(id)

        assertThat(result).isNull()
    }

    @Test
    fun `should find all tasks by userId and return domain entities`() {
        val user = UserFixture.aUser()
        val userIdVO = user.id
        val id = IdValueObject.of(userIdVO.asString()).getValueResult()
        val formattedUserId = UUID.fromString(userIdVO.asString())
        val model = buildTaskModel()

        every { dao.findAllByUserId(formattedUserId) } returns listOf(model)

        val result = adapter.findAllByUserId(id)

        assertThat(result).hasSize(1)
        assertThat(result.first().userId.asString()).isEqualTo(userIdVO.asString())
    }

    @Test
    fun `should return empty list when user has no tasks`() {
        val user = UserFixture.aUser()
        val userIdVO = user.id
        val id = IdValueObject.of(userIdVO.asString()).getValueResult()

        every { dao.findAllByUserId(any()) } returns emptyList()

        val result = adapter.findAllByUserId(id)

        assertThat(result).isEmpty()
    }

    @Test
    fun `should delete a task by id`() {
        val task = TaskFixture.aTask()
        val taskIdVO = task.id
        val id = IdValueObject.of(taskIdVO.asString()).getValueResult()
        val formattedId = UUID.fromString(taskIdVO.asString())

        every { dao.deleteById(any()) } returns Unit

        adapter.deleteById(id)

        verify { dao.deleteById(formattedId) }
    }

    private fun buildTaskModel(): TaskJpaModel {
        val task = TaskFixture.aTask()
        val user = UserFixture.aUser()
        val taskIdVO = task.id
        val userIdVO = user.id
        val taskName = task.taskName.asString()
        val finished = task.finished
        val createdAt = task.createdAt
        val taskId = UUID.fromString(taskIdVO.asString())
        val userId = UUID.fromString(userIdVO.asString())

        return TaskJpaModel(
            id = taskId,
            userId = userId,
            taskName = taskName,
            finished = finished,
            createdAt = createdAt,
            updatedAt = null
        )
    }
}
