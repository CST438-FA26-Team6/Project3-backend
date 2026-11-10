package com.cst438.team6.project3.service

import com.cst438.team6.project3.auth.ProviderIdentity
import com.cst438.team6.project3.config.GoogleOAuthProperties
import com.cst438.team6.project3.user.AuthProvider
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class GoogleIdentityVerifier(
    private val properties: GoogleOAuthProperties
) {

    companion object {
        private const val GOOGLE_JWKS =
            "https://www.googleapis.com/oauth2/v3/certs"

        private val GOOGLE_ISSUERS = setOf(
            "https://accounts.google.com",
            "accounts.google.com"
        )
    }

    private val googleDecoder: NimbusJwtDecoder =
        NimbusJwtDecoder
            .withJwkSetUri(GOOGLE_JWKS)
            .build()
            .also { decoder ->

                val issuerValidator =
                    JwtClaimValidator<String>(
                        "iss"
                    ) { issuer ->
                        issuer in GOOGLE_ISSUERS
                    }

                val audienceValidator =
                    JwtClaimValidator<List<String>>(
                        "aud"
                    ) { audience ->
                        audience?.contains(
                            properties.webClientId
                        ) == true
                    }

                val validator: OAuth2TokenValidator<Jwt> =
                    DelegatingOAuth2TokenValidator(
                        JwtValidators.createDefault(),
                        issuerValidator,
                        audienceValidator
                    )

                decoder.setJwtValidator(validator)
            }

    fun verify(idToken: String): ProviderIdentity {
        val jwt = try {
            googleDecoder.decode(idToken)
        } catch (_: JwtException) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid Google ID token."
            )
        }

        val providerUserId = jwt.subject
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Google account did not provide a subject."
            )

        val email = jwt.getClaimAsString("email")
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Google account did not provide an email."
            )

        val emailVerified =
            jwt.getClaimAsBoolean("email_verified") == true

        val username =
            jwt.getClaimAsString("name")
                ?: email.substringBefore("@")

        return ProviderIdentity(
            provider = AuthProvider.GOOGLE,
            providerUserId = providerUserId,
            username = username,
            email = email,
            emailVerified = emailVerified
        )
    }
}