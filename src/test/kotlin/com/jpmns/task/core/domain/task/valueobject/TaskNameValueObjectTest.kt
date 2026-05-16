package com.jpmns.task.core.domain.task.valueobject

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import com.jpmns.task.shared.fixture.TaskFixture

class TaskNameValueObjectTest {
    @Test
    fun `should create a valid TaskNameValueObject from a valid name`() {
        val task = TaskFixture.aTask()
        val taskName = task.taskName

        val result = TaskNameValueObject.of(taskName.asString())

        assertThat(result.isFail).isFalse()
        assertThat(result.getRealValue().asString()).isEqualTo(taskName.asString())
    }

    @Test
    fun `should accept a task name with exactly 255 characters`() {
        val taskName = "a".repeat(255)

        val result = TaskNameValueObject.of(taskName)

        assertThat(result.isFail).isFalse()
    }

    @Test
    fun `should fail for a task name with more than 255 characters`() {
        val taskName = "a".repeat(256)

        val result = TaskNameValueObject.of(taskName)

        assertThat(result.isFail).isTrue()
    }
}
