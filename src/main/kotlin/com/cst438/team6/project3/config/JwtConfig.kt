package com.cst438.team6.project3.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableConfigurationProperties(
    AppJwtProperties::class,
    AppCorsProperties::class,
    GitHubOAuthProperties::class,
    GoogleOAuthProperties::class
)
class JwtConfig {

    companion object {
        private val APP_JWT_ALGORITHM = MacAlgorithm.HS256
        private const val HMAC_KEY_ALGORITHM = "HmacSHA256"
    }

    @Bean
    fun appJwtKey(properties: AppJwtProperties): SecretKey {
        val decodedSecret = Base64.getDecoder().decode(properties.secretBase64)

        require(decodedSecret.size >= 32) {
            "APP_JWT_SECRET_BASE64 must decode to at least 32 bytes"
        }

        return SecretKeySpec(decodedSecret, HMAC_KEY_ALGORITHM)
    }

    @Bean
    fun jwtDecoder(
        appJwtKey: SecretKey,
        properties: AppJwtProperties
    ): JwtDecoder {
        val decoder = NimbusJwtDecoder
            .withSecretKey(appJwtKey)
            .macAlgorithm(APP_JWT_ALGORITHM)
            .build()

        decoder.setJwtValidator(
            JwtValidators.createDefaultWithIssuer(properties.issuer)
        )

        return decoder
    }

    @Bean
    fun jwtEncoder(appJwtKey: SecretKey): JwtEncoder {
        return NimbusJwtEncoder
            .withSecretKey(appJwtKey)
            .build()
    }
}