package com.jpmns.task.core.presentation.controller

import jakarta.validation.Valid

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDTO
import com.jpmns.task.core.application.usecase.task.dto.input.DeleteTaskInputDTO
import com.jpmns.task.core.application.usecase.task.dto.input.ListTasksInputDTO
import com.jpmns.task.core.application.usecase.task.dto.input.MarkTaskAsFinishedInputDTO
import com.jpmns.task.core.application.usecase.task.dto.input.UpdateTaskInputDTO
import com.jpmns.task.core.application.usecase.task.interfaces.CreateTaskUseCase
import com.jpmns.task.core.application.usecase.task.interfaces.DeleteTaskUseCase
import com.jpmns.task.core.application.usecase.task.interfaces.ListTasksUseCase
import com.jpmns.task.core.application.usecase.task.interfaces.MarkTaskAsFinishedUseCase
import com.jpmns.task.core.application.usecase.task.interfaces.UpdateTaskUseCase
import com.jpmns.task.core.presentation.controller.common.resolver.AuthenticatedUserResolver
import com.jpmns.task.core.presentation.controller.documentation.TaskControllerDoc
import com.jpmns.task.core.presentation.controller.payload.task.request.CreateTaskRequest
import com.jpmns.task.core.presentation.controller.payload.task.request.UpdateTaskRequest
import com.jpmns.task.core.presentation.controller.payload.task.response.TaskResponse

@RestController
@RequestMapping("/api/v1/tasks")
class TaskController(
    private val createTaskUseCase: CreateTaskUseCase,
    private val listTasksUseCase: ListTasksUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val markTaskAsFinishedUseCase: MarkTaskAsFinishedUseCase,
) : TaskControllerDoc {
    @PostMapping
    override fun createTask(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse> {
        logger.info("Create task request received: ${request.taskName}")

        val userId = AuthenticatedUserResolver.getUserId()

        val input = CreateTaskInputDTO(userId = userId, taskName = request.taskName)

        val output = createTaskUseCase.execute(input)

        val response = TaskResponse.of(output)

        logger.info("Task created with id: ${response.id}")
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    override fun listTasks(): ResponseEntity<List<TaskResponse>> {
        logger.info("List tasks request received")

        val userId = AuthenticatedUserResolver.getUserId()

        var input = ListTasksInputDTO(userId = userId)

        val output = listTasksUseCase.execute(input)

        val response = output.map(TaskResponse::of)

        logger.info("Returning ${response.size} tasks")
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{taskId}")
    override fun updateTask(
        @PathVariable taskId: String,
        @Valid @RequestBody request: UpdateTaskRequest,
    ): ResponseEntity<TaskResponse> {
        logger.info("Update task request received for taskId: $taskId")

        val userId = AuthenticatedUserResolver.getUserId()

        val input = UpdateTaskInputDTO(taskId = taskId, userId = userId, taskName = request.taskName)

        val output = updateTaskUseCase.execute(input)

        val response = TaskResponse.of(output)

        logger.info("Task updated: $taskId")
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{taskId}")
    override fun deleteTask(@PathVariable taskId: String): ResponseEntity<Void> {
        logger.info("Delete task request received for taskId: $taskId")

        val userId = AuthenticatedUserResolver.getUserId()

        val input = DeleteTaskInputDTO(taskId = taskId, userId = userId)

        deleteTaskUseCase.execute(input)

        logger.info("Task deleted: $taskId")
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{taskId}/finish")
    override fun markTaskAsFinished(@PathVariable taskId: String): ResponseEntity<Void> {
        logger.info("Mark task as finished request received for taskId: $taskId")

        val userId = AuthenticatedUserResolver.getUserId()

        val input = MarkTaskAsFinishedInputDTO(taskId = taskId, userId = userId)

        markTaskAsFinishedUseCase.execute(input)

        logger.info("Task marked as finished: $taskId")
        return ResponseEntity.noContent().build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskController::class.java)
    }
}
