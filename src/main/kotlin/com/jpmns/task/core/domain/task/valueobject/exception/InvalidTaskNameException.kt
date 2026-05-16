package com.jpmns.task.core.domain.task.valueobject.exception

import com.jpmns.task.core.domain.common.exception.DomainException

class InvalidTaskNameException : DomainException("Task name must be between 1 and 255 characters")
