package com.cst438.team6.project3.user

import com.cst438.team6.project3.TestcontainersConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class UserRepositoryTest(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val identityRepository: IdentityRepository
) {

    @BeforeEach
    fun setUp() {
        identityRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `user can be saved and found by email`() {
        val user = userRepository.save(
            User(
                username = "jessika",
                email = "jessika@example.com"
            )
        )

        val savedUser =
            userRepository.findByEmailIgnoreCase("jessika@example.com")

        assertNotNull(savedUser)
        assertEquals(user.id, savedUser.id)
        assertEquals("jessika", savedUser.username)
        assertEquals("jessika@example.com", savedUser.email)
        assertEquals(UserRole.USER, savedUser.role)
    }

    @Test
    fun `user email lookup is case insensitive`() {
        userRepository.save(
            User(
                username = "jessika",
                email = "jessika@example.com"
            )
        )

        val savedUser =
            userRepository.findByEmailIgnoreCase("JESSIKA@EXAMPLE.COM")

        assertNotNull(savedUser)
        assertEquals("jessika@example.com", savedUser.email)
    }

    @Test
    fun `identity can be saved and found by provider`() {
        val user = userRepository.save(
            User(
                username = "jessika",
                email = "jessika@example.com"
            )
        )

        identityRepository.save(
            Identity(
                user = user,
                provider = AuthProvider.GITHUB,
                providerUserId = "123456"
            )
        )

        val identity =
            identityRepository.findByProviderAndProviderUserId(
                AuthProvider.GITHUB,
                "123456"
            )

        assertNotNull(identity)
        assertEquals(user.id, identity.user.id)
        assertEquals(AuthProvider.GITHUB, identity.provider)
        assertEquals("123456", identity.providerUserId)
    }

    @Test
    fun `user identities can be found by user id`() {
        val user = userRepository.save(
            User(
                username = "jessika",
                email = "jessika@example.com"
            )
        )

        identityRepository.save(
            Identity(
                user = user,
                provider = AuthProvider.GITHUB,
                providerUserId = "123456"
            )
        )

        val identities =
            identityRepository.findByUser_Id(user.id!!)

        assertEquals(1, identities.size)
        assertEquals(AuthProvider.GITHUB, identities[0].provider)
        assertEquals(user.id, identities[0].user.id)
    }
}