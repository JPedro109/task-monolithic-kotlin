package com.jpmns.task.core.domain.task

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

import com.jpmns.task.core.domain.common.exception.DomainException
import com.jpmns.task.shared.fixture.TaskFixture
import com.jpmns.task.shared.fixture.UserFixture

class TaskEntityTest {
    @Test
    fun `should create a task with valid data`() {
        val task = TaskFixture.aTask()
        val id = task.id
        val userId = task.userId
        val taskName = task.taskName
        val finished = task.finished

        val newTask = TaskEntity(
            id = id.asString(),
            userId = userId.asString(),
            taskName = taskName.asString(),
            finished = finished
        )

        assertThat(newTask.id.asString()).isEqualTo(id.asString())
        assertThat(newTask.userId.asString()).isEqualTo(userId.asString())
        assertThat(newTask.taskName.asString()).isEqualTo(taskName.asString())
        assertThat(newTask.finished).isFalse()
        assertThat(newTask.createdAt).isNotNull()
    }

    @Test
    fun `should update the task name`() {
        val task = TaskFixture.aTask()
        val newTaskName = "Updated task name"

        task.updateTaskName(newTaskName)

        assertThat(task.taskName.asString()).isEqualTo(newTaskName)
    }

    @Test
    fun `should mark task as finished`() {
        val task = TaskFixture.aTask()

        task.markAsFinished()

        assertThat(task.finished).isTrue()
    }

    @Test
    fun `should throw when id is not a valid UUID`() {
        val task = TaskFixture.aTask()
        val userId = task.userId
        val taskName = task.taskName
        val finished = task.finished
        val invalidId = "not-a-uuid"

        assertThatThrownBy {
            TaskEntity(
                id = invalidId,
                userId = userId.asString(),
                taskName = taskName.asString(),
                finished = finished
            )
        }.isInstanceOf(DomainException::class.java)
            .satisfies({ ex ->
                val errors = (ex as DomainException).errors
                assertThat(errors).contains("Id is not in format UUID")
            })
    }

    @Test
    fun `should throw when userId is not a valid UUID`() {
        val task = TaskFixture.aTask()
        val id = task.id
        val taskName = task.taskName
        val finished = task.finished
        val invalidUserId = "not-a-uuid"

        assertThatThrownBy {
            TaskEntity(
                id = id.asString(),
                userId = invalidUserId,
                taskName = taskName.asString(),
                finished = finished
            )
        }.isInstanceOf(DomainException::class.java)
    }

    @Test
    fun `should throw when task name exceeds 255 characters`() {
        val task = TaskFixture.aTask()
        val id = task.id
        val user = UserFixture.aUser()
        val userId = user.id
        val finished = task.finished
        val invalidTaskName = "a".repeat(256)

        assertThatThrownBy {
            TaskEntity(
                id = id.asString(),
                userId = userId.asString(),
                taskName = invalidTaskName,
                finished = finished
            )
        }.isInstanceOf(DomainException::class.java)
    }

    @Test
    fun `should throw with two errors when both userId and taskName are invalid`() {
        val task = TaskFixture.aTask()
        val id = task.id
        val finished = task.finished
        val invalidUserId = "not-a-uuid"
        val invalidTaskName = "a".repeat(256)

        assertThatThrownBy {
            TaskEntity(
                id = id.asString(),
                userId = invalidUserId,
                taskName = invalidTaskName,
                finished = finished
            )
        }.isInstanceOf(DomainException::class.java)
    }

    @Test
    fun `should throw when updating with a task name exceeding 255 characters`() {
        val task = TaskFixture.aTask()
        val invalidTaskName = "a".repeat(256)

        assertThatThrownBy {
            task.updateTaskName(invalidTaskName)
        }.isInstanceOf(DomainException::class.java)
    }
}
