package com.cst438.team6.project3

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection(name = "postgresql")
    fun postgresContainer(): PostgreSQLContainer {
        return PostgreSQLContainer("postgres:17-alpine")
    }
}