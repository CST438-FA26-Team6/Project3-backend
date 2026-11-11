package com.cst438.team6.project3.service

import com.cst438.team6.project3.config.GitHubOAuthProperties
import com.cst438.team6.project3.user.AuthProvider
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GitHubIdentityClientTest {

    @Test
    fun `exchange returns identity using verified primary email`() {
        val properties = GitHubOAuthProperties(
            clientId = "github-client",
            clientSecret = "github-secret"
        )

        val builder = RestClient.builder()
        val mockServer = MockRestServiceServer
            .bindTo(builder)
            .build()

        val client = GitHubIdentityClient(
            properties,
            builder
        )

        mockServer.expect(
            requestTo(
                "https://github.com/login/oauth/access_token"
            )
        )
            .andExpect(method(HttpMethod.POST))
            .andExpect(
                header(
                    HttpHeaders.ACCEPT,
                    MediaType.APPLICATION_JSON_VALUE
                )
            )
            .andExpect(
                content().string(
                    containsString("client_id=github-client")
                )
            )
            .andExpect(
                content().string(
                    containsString("client_secret=github-secret")
                )
            )
            .andExpect(
                content().string(
                    containsString("code=github-code")
                )
            )
            .andExpect(
                content().string(
                    containsString("code_verifier=github-verifier")
                )
            )
            .andExpect(
                content().string(
                    containsString(
                        "redirect_uri=http%3A%2F%2Flocalhost%3A8081%2Fauth"
                    )
                )
            )
            .andRespond(
                withSuccess(
                    """
                    {
                      "access_token": "github-access-token"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        mockServer.expect(
            requestTo("https://api.github.com/user")
        )
            .andExpect(method(HttpMethod.GET))
            .andExpect(
                header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer github-access-token"
                )
            )
            .andRespond(
                withSuccess(
                    """
                    {
                      "id": 123456,
                      "login": "jessika-login",
                      "name": "Jessika"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        mockServer.expect(
            requestTo("https://api.github.com/user/emails")
        )
            .andExpect(method(HttpMethod.GET))
            .andExpect(
                header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer github-access-token"
                )
            )
            .andRespond(
                withSuccess(
                    """
                    [
                      {
                        "email": "secondary@example.com",
                        "primary": false,
                        "verified": true
                      },
                      {
                        "email": "jessika@example.com",
                        "primary": true,
                        "verified": true
                      }
                    ]
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        val result = client.exchange(
            code = "github-code",
            codeVerifier = "github-verifier",
            redirectUri = "http://localhost:8081/auth"
        )

        assertEquals(AuthProvider.GITHUB, result.provider)
        assertEquals("123456", result.providerUserId)
        assertEquals("Jessika", result.username)
        assertEquals("jessika@example.com", result.email)
        assertTrue(result.emailVerified)

        mockServer.verify()
    }

    @Test
    fun `exchange uses login when GitHub name is missing`() {
        val properties = GitHubOAuthProperties(
            clientId = "github-client",
            clientSecret = "github-secret"
        )

        val builder = RestClient.builder()
        val mockServer = MockRestServiceServer
            .bindTo(builder)
            .build()

        val client = GitHubIdentityClient(
            properties,
            builder
        )

        mockServer.expect(
            requestTo(
                "https://github.com/login/oauth/access_token"
            )
        )
            .andRespond(
                withSuccess(
                    """
                    {
                      "access_token": "github-access-token"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        mockServer.expect(
            requestTo("https://api.github.com/user")
        )
            .andRespond(
                withSuccess(
                    """
                    {
                      "id": 123456,
                      "login": "jessika-login",
                      "name": null
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        mockServer.expect(
            requestTo("https://api.github.com/user/emails")
        )
            .andRespond(
                withSuccess(
                    """
                    [
                      {
                        "email": "jessika@example.com",
                        "primary": true,
                        "verified": true
                      }
                    ]
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        val result = client.exchange(
            code = "github-code",
            codeVerifier = "github-verifier",
            redirectUri = "http://localhost:8081/auth"
        )

        assertEquals("jessika-login", result.username)

        mockServer.verify()
    }

    @Test
    fun `exchange rejects account without verified primary email`() {
        val properties = GitHubOAuthProperties(
            clientId = "github-client",
            clientSecret = "github-secret"
        )

        val builder = RestClient.builder()
        val mockServer = MockRestServiceServer
            .bindTo(builder)
            .build()

        val client = GitHubIdentityClient(
            properties,
            builder
        )

        mockServer.expect(
            requestTo(
                "https://github.com/login/oauth/access_token"
            )
        )
            .andRespond(
                withSuccess(
                    """
                    {
                      "access_token": "github-access-token"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        mockServer.expect(
            requestTo("https://api.github.com/user")
        )
            .andRespond(
                withSuccess(
                    """
                    {
                      "id": 123456,
                      "login": "jessika-login",
                      "name": "Jessika"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        mockServer.expect(
            requestTo("https://api.github.com/user/emails")
        )
            .andRespond(
                withSuccess(
                    """
                    [
                      {
                        "email": "jessika@example.com",
                        "primary": true,
                        "verified": false
                      }
                    ]
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        val exception = assertThrows<ResponseStatusException> {
            client.exchange(
                code = "github-code",
                codeVerifier = "github-verifier",
                redirectUri = "http://localhost:8081/auth"
            )
        }

        assertEquals(
            "Verify your primary email with GitHub.",
            exception.reason
        )

        mockServer.verify()
    }
}