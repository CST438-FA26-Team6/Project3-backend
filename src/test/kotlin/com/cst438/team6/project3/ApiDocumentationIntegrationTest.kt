package com.cst438.team6.project3

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class ApiDocumentationIntegrationTest(
    @Autowired context: WebApplicationContext
) {
    private val mockMvc: MockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(
            springSecurity()
        )
        .build()

    @Test
    fun `OpenAPI document is publicly available`() {
        mockMvc.perform(get("/openapi.yaml"))
            .andExpect(status().isOk)
            .andExpect(
                content().string(
                    org.hamcrest.Matchers.containsString(
                        "openapi: 3.1.0"
                    )
                )
            )
    }

    @Test
    fun `generated API description is publicly available`() {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.openapi").exists())
            .andExpect(
                jsonPath("$.info.title")
                    .value("Anime and Manga Tracker API")
            )
    }

    @Test
    fun `Swagger UI is publicly available`() {
        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().is3xxRedirection)
    }
}
