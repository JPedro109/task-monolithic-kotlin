package com.jpmns.task.integration.common.sql

import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SqlGroup(
    Sql(
        scripts = ["classpath:/sql/insert-user.sql", "classpath:/sql/insert-task.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    ),
    Sql(
        scripts = ["classpath:/sql/cleanup.sql"],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
)
annotation class SqlCreateSeed
