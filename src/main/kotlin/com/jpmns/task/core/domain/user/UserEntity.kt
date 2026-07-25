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

        val results = listOf(usernameResult, passwordResult)
        validateOrThrow(results)

        this.username = usernameResult.getValueResult()
        this.password = passwordResult.getValueResult()
    }

    fun updateUsername(username: String) {
        val result = UsernameValueObject.of(username).getValueResultOrThrow()

        this.username = result
    }

    fun updatePassword(encodedPassword: String) {
        val result = UserPasswordValueObject.of(encodedPassword).getValueResultOrThrow()

        this.password = result
    }
}
