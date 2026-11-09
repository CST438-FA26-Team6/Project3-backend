package com.cst438.team6.project3

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class PostgresContainerTest {

    @Autowired
    private lateinit var postgres: PostgreSQLContainer

    @Test
    fun `PostgreSQL test container starts`() {
        assertTrue(postgres.isRunning)
    }
}