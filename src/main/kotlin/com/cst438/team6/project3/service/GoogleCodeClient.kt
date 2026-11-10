package com.cst438.team6.project3.service

import com.cst438.team6.project3.auth.GoogleTokenResponse
import com.cst438.team6.project3.config.GoogleOAuthProperties
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.server.ResponseStatusException

@Service
class GoogleCodeClient(
    private val properties: GoogleOAuthProperties,
    restClientBuilder: RestClient.Builder
) {
    companion object {
        private const val TOKEN_URL =
            "https://oauth2.googleapis.com/token"
    }
    private val http = restClientBuilder.build()

    fun exchange(
        code: String,
        codeVerifier: String,
        redirectUri: String
    ): String {
        val form = LinkedMultiValueMap<String, String>()

        form.add("code", code)
        form.add("client_id", properties.webClientId)
        form.add("client_secret", properties.webClientSecret)
        form.add("redirect_uri", redirectUri)
        form.add("grant_type", "authorization_code")
        form.add("code_verifier", codeVerifier)

        val response = http.post()
            .uri(TOKEN_URL)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(form)
            .retrieve()
            .body(GoogleTokenResponse::class.java)
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Google token exchange failed."
            )
        return response.idToken
    }
}