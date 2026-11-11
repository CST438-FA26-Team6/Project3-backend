package com.cst438.team6.project3.auth

import com.cst438.team6.project3.TestcontainersConfiguration
import com.cst438.team6.project3.service.AccountService
import com.cst438.team6.project3.service.AppTokenService
import com.cst438.team6.project3.service.GitHubIdentityClient
import com.cst438.team6.project3.service.GoogleCodeClient
import com.cst438.team6.project3.service.GoogleIdentityVerifier
import com.cst438.team6.project3.user.AuthProvider
import com.cst438.team6.project3.user.User
import com.cst438.team6.project3.user.UserRole
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.UUID

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class AuthControllerSuccessTest(
    @Autowired context: WebApplicationContext
) {

    @MockitoBean
    private lateinit var gitHubIdentityClient: GitHubIdentityClient

    @MockitoBean
    private lateinit var googleCodeClient: GoogleCodeClient

    @MockitoBean
    private lateinit var googleIdentityVerifier: GoogleIdentityVerifier

    @MockitoBean
    private lateinit var accountService: AccountService

    @MockitoBean
    private lateinit var appTokenService: AppTokenService

    private val mockMvc: MockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(
            springSecurity()
        )
        .build()

    @Test
    fun `github authentication returns application token and user`() {
        val identity = ProviderIdentity(
            provider = AuthProvider.GITHUB,
            providerUserId = "github-123",
            username = "jessika",
            email = "jessika@example.com",
            emailVerified = true
        )

        val user = User(
            username = "jessika",
            email = "jessika@example.com",
            role = UserRole.USER,
            id = UUID.randomUUID()
        )

        `when`(
            gitHubIdentityClient.exchange(
                "github-code",
                "github-verifier",
                "http://localhost:8081/auth"
            )
        ).thenReturn(identity)

        `when`(
            accountService.findOrCreate(identity)
        ).thenReturn(user)

        `when`(
            appTokenService.issueFor(user)
        ).thenReturn("app-jwt")

        mockMvc.perform(
            post("/auth/github")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "code": "github-code",
                      "codeVerifier": "github-verifier",
                      "redirectUri": "http://localhost:8081/auth"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("app-jwt"))
            .andExpect(jsonPath("$.user.id").value(user.id.toString()))
            .andExpect(jsonPath("$.user.username").value("jessika"))
            .andExpect(jsonPath("$.user.email").value("jessika@example.com"))
            .andExpect(jsonPath("$.user.role").value("USER"))

        verify(gitHubIdentityClient).exchange(
            "github-code",
            "github-verifier",
            "http://localhost:8081/auth"
        )

        verify(accountService).findOrCreate(identity)
        verify(appTokenService).issueFor(user)
    }

    @Test
    fun `google web authentication exchanges code and returns application token`() {
        val identity = ProviderIdentity(
            provider = AuthProvider.GOOGLE,
            providerUserId = "google-123",
            username = "jessika",
            email = "jessika@example.com",
            emailVerified = true
        )

        val user = User(
            username = "jessika",
            email = "jessika@example.com",
            role = UserRole.USER,
            id = UUID.randomUUID()
        )

        `when`(
            googleCodeClient.exchange(
                "google-code",
                "google-verifier",
                "http://localhost:8081/auth"
            )
        ).thenReturn("google-id-token")

        `when`(
            googleIdentityVerifier.verify("google-id-token")
        ).thenReturn(identity)

        `when`(
            accountService.findOrCreate(identity)
        ).thenReturn(user)

        `when`(
            appTokenService.issueFor(user)
        ).thenReturn("app-jwt")

        mockMvc.perform(
            post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "code": "google-code",
                      "codeVerifier": "google-verifier",
                      "redirectUri": "http://localhost:8081/auth"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("app-jwt"))
            .andExpect(jsonPath("$.user.email").value("jessika@example.com"))
            .andExpect(jsonPath("$.user.role").value("USER"))

        verify(googleCodeClient).exchange(
            "google-code",
            "google-verifier",
            "http://localhost:8081/auth"
        )

        verify(googleIdentityVerifier).verify("google-id-token")
        verify(accountService).findOrCreate(identity)
        verify(appTokenService).issueFor(user)
    }

    @Test
    fun `google android authentication accepts id token directly`() {
        val identity = ProviderIdentity(
            provider = AuthProvider.GOOGLE,
            providerUserId = "google-123",
            username = "jessika",
            email = "jessika@example.com",
            emailVerified = true
        )

        val user = User(
            username = "jessika",
            email = "jessika@example.com",
            role = UserRole.USER,
            id = UUID.randomUUID()
        )

        `when`(
            googleIdentityVerifier.verify("android-google-id-token")
        ).thenReturn(identity)

        `when`(
            accountService.findOrCreate(identity)
        ).thenReturn(user)

        `when`(
            appTokenService.issueFor(user)
        ).thenReturn("app-jwt")

        mockMvc.perform(
            post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "idToken": "android-google-id-token"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("app-jwt"))
            .andExpect(jsonPath("$.user.username").value("jessika"))
            .andExpect(jsonPath("$.user.email").value("jessika@example.com"))

        verify(googleIdentityVerifier).verify(
            "android-google-id-token"
        )

        verify(accountService).findOrCreate(identity)
        verify(appTokenService).issueFor(user)
    }
}