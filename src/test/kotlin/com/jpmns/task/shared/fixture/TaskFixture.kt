package com.jpmns.task.shared.fixture

import com.jpmns.task.core.domain.task.TaskEntity

object TaskFixture {
    private const val DEFAULT_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901"
    private const val DEFAULT_USER_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901"
    private const val DEFAULT_TASK_NAME = "Buy groceries"
    private const val DEFAULT_FINISHED = false

    fun aTask(): TaskEntity = TaskEntity(
        id = DEFAULT_ID,
        userId = DEFAULT_USER_ID,
        taskName = DEFAULT_TASK_NAME,
        finished = DEFAULT_FINISHED
    )

    fun aTaskWithName(taskName: String): TaskEntity = TaskEntity(
        id = DEFAULT_ID,
        userId = DEFAULT_USER_ID,
        taskName = taskName,
        finished = DEFAULT_FINISHED
    )
}
