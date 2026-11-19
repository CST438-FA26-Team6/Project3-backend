package com.cst438.team6.project3.user

import com.cst438.team6.project3.TestcontainersConfiguration
import com.cst438.team6.project3.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.UUID

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class UserControllerTest(
    @Autowired context: WebApplicationContext
) {

    @MockitoBean
    private lateinit var userService: UserService

    private val mockMvc: MockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply<
                org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
                >(
            springSecurity()
        )
        .build()

    @Test
    fun `get profile returns authenticated user`() {
        val userId = UUID.randomUUID()

        `when`(
            userService.getProfile(userId)
        ).thenReturn(
            UserProfileResponse(
                id = userId,
                username = "user",
                email = "user@example.com",
                role = UserRole.USER
            )
        )

        mockMvc.perform(
            get("/api/users/me")
                .with(
                    jwt().jwt { token ->
                        token.subject(userId.toString())
                    }
                )
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.id")
                    .value(userId.toString())
            )
            .andExpect(
                jsonPath("$.username")
                    .value("user")
            )
            .andExpect(
                jsonPath("$.email")
                    .value("user@example.com")
            )
            .andExpect(
                jsonPath("$.role")
                    .value("USER")
            )

        verify(userService)
            .getProfile(userId)
    }

    @Test
    fun `get profile without authentication returns 401`() {
        mockMvc.perform(
            get("/api/users/me")
        )
            .andExpect(
                status().isUnauthorized
            )
    }

    @Test
    fun `update profile changes authenticated user`() {
        val userId = UUID.randomUUID()

        `when`(
            userService.updateProfile(
                userId,
                UpdateUserProfileRequest(
                    username = "new-user"
                )
            )
        ).thenReturn(
            UserProfileResponse(
                id = userId,
                username = "new-user",
                email = "user@example.com",
                role = UserRole.USER
            )
        )

        mockMvc.perform(
            patch("/api/users/me")
                .with(
                    jwt().jwt { token ->
                        token.subject(userId.toString())
                    }
                )
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(
                    """
                    {
                      "username": "new-user"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.username")
                    .value("new-user")
            )
            .andExpect(
                jsonPath("$.email")
                    .value("user@example.com")
            )
    }

    @Test
    fun `delete account deletes authenticated user`() {
        val userId = UUID.randomUUID()

        mockMvc.perform(
            delete("/api/users/me")
                .with(
                    jwt().jwt { token ->
                        token.subject(userId.toString())
                    }
                )
        )
            .andExpect(
                status().isNoContent
            )

        verify(userService)
            .deleteAccount(userId)
    }

    @Test
    fun `invalid token subject returns 401`() {
        mockMvc.perform(
            get("/api/users/me")
                .with(
                    jwt().jwt { token ->
                        token.subject("not-a-uuid")
                    }
                )
        )
            .andExpect(
                status().isUnauthorized
            )
    }
}