package com.jpmns.task

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

import com.jpmns.task.configuration.security.SecurityConfigProperties

@SpringBootApplication
@EnableConfigurationProperties(SecurityConfigProperties::class)
class TaskApplication

fun main(args: Array<String>) {
    runApplication<TaskApplication>(*args)
}
