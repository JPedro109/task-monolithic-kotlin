package com.jpmns.task.core.domain.user.valueobject.exception

import com.jpmns.task.core.domain.common.exception.DomainException

class InvalidUsernameException : DomainException(
    "Username must be between 3 and 50 characters and contain only letters, numbers or underscores"
)
