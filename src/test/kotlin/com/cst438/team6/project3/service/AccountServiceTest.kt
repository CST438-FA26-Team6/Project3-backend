package com.cst438.team6.project3.service

import com.cst438.team6.project3.auth.ProviderIdentity
import com.cst438.team6.project3.user.AuthProvider
import com.cst438.team6.project3.user.Identity
import com.cst438.team6.project3.user.IdentityRepository
import com.cst438.team6.project3.user.User
import com.cst438.team6.project3.user.UserRepository
import com.cst438.team6.project3.user.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID
import kotlin.test.assertEquals

class AccountServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var identityRepository: IdentityRepository
    private lateinit var accountService: AccountService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        identityRepository = mock(IdentityRepository::class.java)

        accountService = AccountService(
            userRepository,
            identityRepository
        )
    }

    @Test
    fun `existing provider identity returns existing user`() {
        val user = User(
            username = "user",
            email = "user@example.com",
            id = UUID.randomUUID()
        )

        val savedIdentity = Identity(
            user = user,
            provider = AuthProvider.GITHUB,
            providerUserId = "123456",
            id = UUID.randomUUID()
        )

        `when`(
            identityRepository.findByProviderAndProviderUserId(
                AuthProvider.GITHUB,
                "123456"
            )
        ).thenReturn(savedIdentity)

        val result = accountService.findOrCreate(
            ProviderIdentity(
                provider = AuthProvider.GITHUB,
                providerUserId = "123456",
                username = "user",
                email = "user@example.com",
                emailVerified = true
            )
        )

        assertEquals(user.id, result.id)
        assertEquals("user@example.com", result.email)

        verify(userRepository, never()).save(any())
        verify(identityRepository, never()).save(any())
    }

    @Test
    fun `unverified email returns bad request`() {
        `when`(
            identityRepository.findByProviderAndProviderUserId(
                AuthProvider.GITHUB,
                "123456"
            )
        ).thenReturn(null)

        val exception = assertThrows<ResponseStatusException> {
            accountService.findOrCreate(
                ProviderIdentity(
                    provider = AuthProvider.GITHUB,
                    providerUserId = "123456",
                    username = "user",
                    email = "user@example.com",
                    emailVerified = false
                )
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)

        verifyNoInteractions(userRepository)
        verify(identityRepository, never()).save(any())
    }

    @Test
    fun `new verified identity creates user and identity`() {
        val userId = UUID.randomUUID()

        `when`(
            identityRepository.findByProviderAndProviderUserId(
                AuthProvider.GITHUB,
                "123456"
            )
        ).thenReturn(null)

        `when`(
            userRepository.findByEmailIgnoreCase(
                "user@example.com"
            )
        ).thenReturn(null)

        `when`(
            userRepository.save(any(User::class.java))
        ).thenAnswer { invocation ->
            User(
                username = invocation.getArgument<User>(0).username,
                email = invocation.getArgument<User>(0).email,
                role = UserRole.USER,
                id = userId
            )
        }

        val result = accountService.findOrCreate(
            ProviderIdentity(
                provider = AuthProvider.GITHUB,
                providerUserId = "123456",
                username = "user",
                email = "  USER@EXAMPLE.COM  ",
                emailVerified = true
            )
        )

        assertEquals(userId, result.id)
        assertEquals("user", result.username)
        assertEquals("user@example.com", result.email)
        assertEquals(UserRole.USER, result.role)

        verify(userRepository).save(any(User::class.java))
        verify(identityRepository).save(any(Identity::class.java))
    }

    @Test
    fun `email registered with another provider returns conflict`() {
        val userId = UUID.randomUUID()

        val existingUser = User(
            username = "user",
            email = "user@example.com",
            id = userId
        )

        val googleIdentity = Identity(
            user = existingUser,
            provider = AuthProvider.GOOGLE,
            providerUserId = "google-123",
            id = UUID.randomUUID()
        )

        `when`(
            identityRepository.findByProviderAndProviderUserId(
                AuthProvider.GITHUB,
                "github-123"
            )
        ).thenReturn(null)

        `when`(
            userRepository.findByEmailIgnoreCase(
                "user@example.com"
            )
        ).thenReturn(existingUser)

        `when`(
            identityRepository.findByUser_Id(userId)
        ).thenReturn(listOf(googleIdentity))

        val exception = assertThrows<ResponseStatusException> {
            accountService.findOrCreate(
                ProviderIdentity(
                    provider = AuthProvider.GITHUB,
                    providerUserId = "github-123",
                    username = "user",
                    email = "user@example.com",
                    emailVerified = true
                )
            )
        }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)

        assertEquals(
            "This email is already registered with google.",
            exception.reason
        )

        verify(userRepository, never()).save(any())
        verify(identityRepository, never()).save(any())
    }
}