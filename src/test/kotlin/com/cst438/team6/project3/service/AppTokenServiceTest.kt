package com.cst438.team6.project3.service

import com.cst438.team6.project3.TestcontainersConfiguration
import com.cst438.team6.project3.user.User
import com.cst438.team6.project3.user.UserRole
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class AppTokenServiceTest(
    @Autowired private val appTokenService: AppTokenService,
    @Autowired private val jwtDecoder: JwtDecoder
) {

    @Test
    fun `issued token contains expected user claims`() {
        val userId = UUID.randomUUID()

        val user = User(
            username = "jessika",
            email = "jessika@example.com",
            role = UserRole.USER,
            id = userId
        )

        val token = appTokenService.issueFor(user)

        val jwt = jwtDecoder.decode(token)

        assertEquals(userId.toString(), jwt.subject)
        assertEquals("jessika@example.com", jwt.getClaimAsString("email"))
        assertEquals(
            listOf("USER"),
            jwt.getClaimAsStringList("roles")
        )

        assertNotNull(jwt.issuedAt)
        assertNotNull(jwt.expiresAt)

        assertTrue(
            jwt.expiresAt!!.isAfter(jwt.issuedAt)
        )
    }

    @Test
    fun `admin token contains admin role`() {
        val user = User(
            username = "admin",
            email = "admin@example.com",
            role = UserRole.ADMIN,
            id = UUID.randomUUID()
        )

        val token = appTokenService.issueFor(user)

        val jwt = jwtDecoder.decode(token)

        assertEquals(
            listOf("ADMIN"),
            jwt.getClaimAsStringList("roles")
        )
    }

    @Test
    fun `unsaved user cannot receive token`() {
        val user = User(
            username = "jessika",
            email = "jessika@example.com"
        )

        val exception = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            appTokenService.issueFor(user)
        }

        assertEquals(
            "User must be saved before a token can be issued.",
            exception.message
        )
    }
}