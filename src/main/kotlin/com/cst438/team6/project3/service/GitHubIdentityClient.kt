package com.cst438.team6.project3.service

import com.cst438.team6.project3.auth.GitHubEmailResponse
import com.cst438.team6.project3.auth.GitHubTokenResponse
import com.cst438.team6.project3.auth.GitHubUserResponse
import com.cst438.team6.project3.auth.ProviderIdentity
import com.cst438.team6.project3.config.GitHubOAuthProperties
import com.cst438.team6.project3.user.AuthProvider
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class GitHubIdentityClient(
    private val properties: GitHubOAuthProperties,
    restClientBuilder: RestClient.Builder
) {

    companion object {
        private const val TOKEN_URL =
            "https://github.com/login/oauth/access_token"

        private const val USER_URL =
            "https://api.github.com/user"

        private const val EMAILS_URL =
            "https://api.github.com/user/emails"
    }

    private val http = restClientBuilder.build()

    fun exchange(
        code: String,
        codeVerifier: String,
        redirectUri: String
    ): ProviderIdentity {
        val form = LinkedMultiValueMap<String, String>()

        form.add("client_id", properties.clientId)
        form.add("client_secret", properties.clientSecret)
        form.add("code", code)
        form.add("code_verifier", codeVerifier)
        form.add("redirect_uri", redirectUri)

        val token = http.post()
            .uri(TOKEN_URL)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(GitHubTokenResponse::class.java)
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "GitHub token exchange failed."
            )

        val user = getUser(token.accessToken)

        val emails = getEmails(token.accessToken)

        val primaryEmail = emails.firstOrNull {
            it.primary && it.verified
        } ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Verify your primary email with GitHub."
        )

        return ProviderIdentity(
            provider = AuthProvider.GITHUB,
            providerUserId = user.id.toString(),
            username = user.name ?: user.login,
            email = primaryEmail.email,
            emailVerified = true
        )
    }

    private fun getUser(accessToken: String): GitHubUserResponse {
        return http.get()
            .uri(USER_URL)
            .headers {
                it.setBearerAuth(accessToken)
                it.accept = listOf(MediaType.APPLICATION_JSON)
            }
            .retrieve()
            .body(GitHubUserResponse::class.java)
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Unable to retrieve GitHub user."
            )
    }

    private fun getEmails(
        accessToken: String
    ): List<GitHubEmailResponse> {
        return http.get()
            .uri(EMAILS_URL)
            .headers {
                it.setBearerAuth(accessToken)
                it.accept = listOf(MediaType.APPLICATION_JSON)
            }
            .retrieve()
            .body(
                object :
                    ParameterizedTypeReference<List<GitHubEmailResponse>>() {}
            )
            ?: emptyList()
    }
}