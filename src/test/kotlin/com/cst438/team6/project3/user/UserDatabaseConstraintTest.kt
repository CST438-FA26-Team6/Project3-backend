package com.cst438.team6.project3.user

import com.cst438.team6.project3.TestcontainersConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertEquals

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class UserDatabaseConstraintTest(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val identityRepository: IdentityRepository
) {

    @BeforeEach
    fun setUp() {
        identityRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `duplicate user email is rejected`() {
        userRepository.saveAndFlush(
            User(
                username = "first-user",
                email = "same@example.com"
            )
        )

        assertThrows<DataIntegrityViolationException> {
            userRepository.saveAndFlush(
                User(
                    username = "second-user",
                    email = "same@example.com"
                )
            )
        }
    }

    @Test
    fun `duplicate provider identity is rejected`() {
        val firstUser = userRepository.saveAndFlush(
            User(
                username = "first-user",
                email = "first@example.com"
            )
        )

        val secondUser = userRepository.saveAndFlush(
            User(
                username = "second-user",
                email = "second@example.com"
            )
        )

        identityRepository.saveAndFlush(
            Identity(
                user = firstUser,
                provider = AuthProvider.GITHUB,
                providerUserId = "github-123"
            )
        )

        assertThrows<DataIntegrityViolationException> {
            identityRepository.saveAndFlush(
                Identity(
                    user = secondUser,
                    provider = AuthProvider.GITHUB,
                    providerUserId = "github-123"
                )
            )
        }
    }

    @Test
    fun `one user can have GitHub and Google identities`() {
        val user = userRepository.saveAndFlush(
            User(
                username = "jessika",
                email = "jessika@example.com"
            )
        )

        identityRepository.saveAllAndFlush(
            listOf(
                Identity(
                    user = user,
                    provider = AuthProvider.GITHUB,
                    providerUserId = "github-123"
                ),
                Identity(
                    user = user,
                    provider = AuthProvider.GOOGLE,
                    providerUserId = "google-123"
                )
            )
        )

        val identities =
            identityRepository.findByUser_Id(user.id!!)

        assertEquals(2, identities.size)
        assertEquals(
            setOf(
                AuthProvider.GITHUB,
                AuthProvider.GOOGLE
            ),
            identities.map { it.provider }.toSet()
        )
    }
}