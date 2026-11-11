package com.cst438.team6.project3.service

import com.cst438.team6.project3.config.GoogleOAuthProperties
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import kotlin.test.assertEquals

class GoogleCodeClientTest {

    @Test
    fun `exchange returns Google ID token`() {
        val properties = GoogleOAuthProperties(
            webClientId = "google-client",
            webClientSecret = "google-secret"
        )

        val builder = RestClient.builder()
        val mockServer = MockRestServiceServer
            .bindTo(builder)
            .build()

        val client = GoogleCodeClient(
            properties,
            builder
        )

        mockServer.expect(
            requestTo("https://oauth2.googleapis.com/token")
        )
            .andExpect(method(HttpMethod.POST))
            .andExpect(
                content().string(
                    containsString("code=google-code")
                )
            )
            .andExpect(
                content().string(
                    containsString("client_id=google-client")
                )
            )
            .andExpect(
                content().string(
                    containsString("client_secret=google-secret")
                )
            )
            .andExpect(
                content().string(
                    containsString("grant_type=authorization_code")
                )
            )
            .andExpect(
                content().string(
                    containsString("code_verifier=google-verifier")
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
                      "id_token": "google-id-token"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        val result = client.exchange(
            code = "google-code",
            codeVerifier = "google-verifier",
            redirectUri = "http://localhost:8081/auth"
        )

        assertEquals("google-id-token", result)

        mockServer.verify()
    }
}