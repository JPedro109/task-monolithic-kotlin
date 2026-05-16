package com.jpmns.task.core.domain.user.valueobject.exception

import com.jpmns.task.core.domain.common.exception.DomainException

class InvalidPasswordException : DomainException("Password must be at least 8 characters long")
