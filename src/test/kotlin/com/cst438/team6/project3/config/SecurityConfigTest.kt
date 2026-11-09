package com.cst438.team6.project3.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import com.cst438.team6.project3.TestcontainersConfiguration
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class SecurityConfigTest {

    private val mockMvc: MockMvc

    constructor(@Autowired context: WebApplicationContext) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(
                springSecurity()
            )
            .build()
    }

    @Test
    fun `protected endpoint without token returns 401`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `CORS preflight from Expo web origin succeeds`() {
        mockMvc.perform(
            options("/")
                .header("Origin", "http://localhost:8081")
                .header("Access-Control-Request-Method", "GET")
        )
            .andExpect(status().isOk)
            .andExpect(
                header().string(
                    "Access-Control-Allow-Origin",
                    "http://localhost:8081"
                )
            )
            .andExpect(
                header().string(
                    "Access-Control-Allow-Methods",
                    "GET,POST,PUT,PATCH,DELETE,OPTIONS"
                )
            )
    }
}