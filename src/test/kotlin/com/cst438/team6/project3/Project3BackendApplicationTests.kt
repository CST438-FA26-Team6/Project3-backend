package com.cst438.team6.project3

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class Project3BackendApplicationTests {

	@Test
	fun contextLoads() {
	}

}
