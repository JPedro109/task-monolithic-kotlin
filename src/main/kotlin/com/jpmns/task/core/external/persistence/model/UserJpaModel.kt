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
@Table(name = "users")
@Suppress("UseDataClass")
class UserJpaModel(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID,
    @Column(nullable = false, unique = true, length = 50)
    var username: String,
    @Column(nullable = false)
    var password: String,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
)
