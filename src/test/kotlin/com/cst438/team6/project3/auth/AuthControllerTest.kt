package com.cst438.team6.project3.auth

import com.cst438.team6.project3.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class AuthControllerTest(
    @Autowired context: WebApplicationContext
) {

    private val mockMvc: MockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(
            springSecurity()
        )
        .build()

    @Test
    fun `github auth endpoint is publicly accessible`() {
        mockMvc.perform(
            post("/auth/github")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "code": "",
                      "codeVerifier": "",
                      "redirectUri": ""
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `google auth endpoint is publicly accessible`() {
        mockMvc.perform(
            post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "code": "",
                      "codeVerifier": "",
                      "redirectUri": ""
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `github auth rejects incomplete request`() {
        mockMvc.perform(
            post("/auth/github")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "code": "test-code",
                      "codeVerifier": "",
                      "redirectUri": ""
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `google auth rejects incomplete web request`() {
        mockMvc.perform(
            post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "code": "test-code"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `protected endpoint still requires authentication`() {
        mockMvc.perform(
            get("/api/titles")
                .param("query", "cowboy")
        )
            .andExpect(status().isUnauthorized)
    }
}