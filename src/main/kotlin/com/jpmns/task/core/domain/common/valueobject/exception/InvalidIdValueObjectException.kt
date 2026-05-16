package com.jpmns.task.core.domain.common.valueobject.exception

import com.jpmns.task.core.domain.common.exception.DomainException

class InvalidIdValueObjectException : DomainException("Id is not in format UUID")
