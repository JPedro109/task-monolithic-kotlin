package com.jpmns.task.core.domain.user

import java.time.Instant

import com.jpmns.task.core.domain.common.abstracts.Entity
import com.jpmns.task.core.domain.user.valueobject.UserPasswordValueObject
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject

class UserEntity(
    id: String,
    username: String,
    password: String,
    createdAt: Instant? = null,
    val updatedAt: Instant? = null
) : Entity(id, createdAt) {
    var username: UsernameValueObject
        private set

    var password: UserPasswordValueObject
        private set

    init {
        val usernameResult = UsernameValueObject.of(username)
        val passwordResult = UserPasswordValueObject.of(password)
        validateOrThrow(listOf(usernameResult, passwordResult))

        this.username = usernameResult.getRealValue()
        this.password = passwordResult.getRealValue()
    }

    fun updateUsername(username: String) {
        val result = UsernameValueObject.of(username)

        validateOrThrow(listOf(result))

        this.username = result.getRealValue()
    }

    fun updatePassword(encodedPassword: String) {
        val result = UserPasswordValueObject.of(encodedPassword)

        validateOrThrow(listOf(result))

        this.password = result.getRealValue()
    }
}
