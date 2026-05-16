package com.jpmns.task.core.external.persistence.model

import java.time.Instant
import java.util.UUID

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Table(name = "tasks")
@Suppress("UseDataClass")
class TaskJpaModel(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    var userId: UUID,
    @Column(name = "task_name", nullable = false)
    var taskName: String,
    @Column(nullable = false)
    var finished: Boolean,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
)
