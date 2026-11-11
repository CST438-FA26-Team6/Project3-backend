package com.cst438.team6.project3.auth

import com.cst438.team6.project3.service.AccountService
import com.cst438.team6.project3.service.AppTokenService
import com.cst438.team6.project3.service.GitHubIdentityClient
import com.cst438.team6.project3.service.GoogleCodeClient
import com.cst438.team6.project3.service.GoogleIdentityVerifier
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/auth")
class AuthController(
    private val gitHubIdentityClient: GitHubIdentityClient,
    private val googleCodeClient: GoogleCodeClient,
    private val googleIdentityVerifier: GoogleIdentityVerifier,
    private val accountService: AccountService,
    private val appTokenService: AppTokenService
) {

    @PostMapping("/github")
    fun github(
        @RequestBody request: CodeExchangeRequest
    ): AuthResponse {
        validateGitHubRequest(request)

        val identity = gitHubIdentityClient.exchange(
            code = request.code,
            codeVerifier = request.codeVerifier,
            redirectUri = request.redirectUri
        )

        return finishAuthentication(identity)
    }

    @PostMapping("/google")
    fun google(
        @RequestBody request: GoogleAuthRequest
    ): AuthResponse {
        val identity = if (!request.idToken.isNullOrBlank()) {
            // Android: Google native sign-in returns the ID token directly.
            googleIdentityVerifier.verify(request.idToken)
        } else {
            // Web: exchange the OAuth authorization code first.
            val code = request.code
            val codeVerifier = request.codeVerifier
            val redirectUri = request.redirectUri

            if (
                code.isNullOrBlank() ||
                codeVerifier.isNullOrBlank() ||
                redirectUri.isNullOrBlank()
            ) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "idToken or code, codeVerifier, and redirectUri are required."
                )
            }

            val idToken = googleCodeClient.exchange(
                code = code,
                codeVerifier = codeVerifier,
                redirectUri = redirectUri
            )

            googleIdentityVerifier.verify(idToken)
        }

        return finishAuthentication(identity)
    }

    private fun finishAuthentication(
        identity: ProviderIdentity
    ): AuthResponse {
        val user = accountService.findOrCreate(identity)
        val token = appTokenService.issueFor(user)

        return AuthResponse(
            token = token,
            user = UserResponse.from(user)
        )
    }

    private fun validateGitHubRequest(
        request: CodeExchangeRequest
    ) {
        if (
            request.code.isBlank() ||
            request.codeVerifier.isBlank() ||
            request.redirectUri.isBlank()
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "code, codeVerifier, and redirectUri are required."
            )
        }
    }
}