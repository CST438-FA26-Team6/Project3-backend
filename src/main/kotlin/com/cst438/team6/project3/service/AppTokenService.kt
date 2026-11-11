package com.cst438.team6.project3.service

import com.cst438.team6.project3.config.AppJwtProperties
import com.cst438.team6.project3.user.User
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AppTokenService(
    private val jwtEncoder: JwtEncoder,
    private val jwtProperties: AppJwtProperties
) {

    fun issueFor(user: User): String {
        val userId = requireNotNull(user.id) {
            "User must be saved before a token can be issued."
        }

        val now = Instant.now()

        val claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiresAt(now.plus(jwtProperties.ttl))
            .subject(userId.toString())
            .claim("email", user.email)
            .claim("roles", listOf(user.role.name))
            .build()

        val header = JwsHeader
            .with(MacAlgorithm.HS256)
            .build()

        return jwtEncoder
            .encode(
                JwtEncoderParameters.from(
                    header,
                    claims
                )
            )
            .tokenValue
    }
}