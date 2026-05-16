package com.jpmns.task.shared.security

import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithSecurityContext

import com.jpmns.task.shared.security.factory.WithMockJwtTokenSecurityContextFactory

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockJwtTokenSecurityContextFactory::class)
annotation class WithJwtTokenMock(
    val sub: String = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    val httpStatus: HttpStatus = HttpStatus.OK
)
