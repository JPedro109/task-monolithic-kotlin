package com.jpmns.task.shared.fixture

import com.jpmns.task.core.domain.user.UserEntity

object UserFixture {
    private const val DEFAULT_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    private const val DEFAULT_USERNAME = "john_doe"
    private const val DEFAULT_PASSWORD = "password"

    fun aUser(): UserEntity = UserEntity(
        id = DEFAULT_ID,
        username = DEFAULT_USERNAME,
        password = DEFAULT_PASSWORD
    )
}
