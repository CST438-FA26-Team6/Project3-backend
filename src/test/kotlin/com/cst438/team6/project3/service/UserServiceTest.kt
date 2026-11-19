package com.cst438.team6.project3.service

import com.cst438.team6.project3.library.LibraryEntryRepository
import com.cst438.team6.project3.user.IdentityRepository
import com.cst438.team6.project3.user.UpdateUserProfileRequest
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
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var identityRepository: IdentityRepository
    private lateinit var libraryEntryRepository: LibraryEntryRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository =
            mock(UserRepository::class.java)

        identityRepository =
            mock(IdentityRepository::class.java)

        libraryEntryRepository =
            mock(LibraryEntryRepository::class.java)

        userService = UserService(
            userRepository,
            identityRepository,
            libraryEntryRepository
        )
    }

    @Test
    fun `get profile returns authenticated user`() {
        val userId = UUID.randomUUID()

        val user = User(
            username = "user",
            email = "user@example.com",
            role = UserRole.USER,
            id = userId
        )

        `when`(
            userRepository.findById(userId)
        ).thenReturn(Optional.of(user))

        val result =
            userService.getProfile(userId)

        assertEquals(userId, result.id)
        assertEquals("user", result.username)
        assertEquals(
            "user@example.com",
            result.email
        )
        assertEquals(
            UserRole.USER,
            result.role
        )
    }

    @Test
    fun `get profile returns not found when user does not exist`() {
        val userId = UUID.randomUUID()

        `when`(
            userRepository.findById(userId)
        ).thenReturn(Optional.empty())

        val exception =
            assertThrows<ResponseStatusException> {
                userService.getProfile(userId)
            }

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.statusCode
        )
    }

    @Test
    fun `update profile changes username`() {
        val userId = UUID.randomUUID()

        val user = User(
            username = "old-user",
            email = "user@example.com",
            role = UserRole.USER,
            id = userId
        )

        `when`(
            userRepository.findById(userId)
        ).thenReturn(Optional.of(user))

        `when`(
            userRepository.save(
                any(User::class.java)
            )
        ).thenAnswer { invocation ->
            invocation.getArgument<User>(0)
        }

        val result =
            userService.updateProfile(
                userId,
                UpdateUserProfileRequest(
                    username = "  new-user  "
                )
            )

        assertEquals(
            "new-user",
            result.username
        )
        assertEquals(
            "user@example.com",
            result.email
        )
        assertEquals(
            UserRole.USER,
            result.role
        )

        verify(
            userRepository
        ).save(user)
    }

    @Test
    fun `update profile rejects blank username`() {
        val userId = UUID.randomUUID()

        val user = User(
            username = "user",
            email = "user@example.com",
            id = userId
        )

        `when`(
            userRepository.findById(userId)
        ).thenReturn(Optional.of(user))

        val exception =
            assertThrows<ResponseStatusException> {
                userService.updateProfile(
                    userId,
                    UpdateUserProfileRequest(
                        username = "   "
                    )
                )
            }

        assertEquals(
            HttpStatus.BAD_REQUEST,
            exception.statusCode
        )

        verify(
            userRepository,
            never()
        ).save(any(User::class.java))
    }

    @Test
    fun `delete account removes library identities and user`() {
        val userId = UUID.randomUUID()

        val user = User(
            username = "user",
            email = "user@example.com",
            id = userId
        )

        `when`(
            userRepository.findById(userId)
        ).thenReturn(Optional.of(user))

        userService.deleteAccount(userId)

        verify(
            libraryEntryRepository
        ).deleteByUser_Id(userId)

        verify(
            identityRepository
        ).deleteByUser_Id(userId)

        verify(
            userRepository
        ).delete(user)
    }

    @Test
    fun `delete account returns not found when user does not exist`() {
        val userId = UUID.randomUUID()

        `when`(
            userRepository.findById(userId)
        ).thenReturn(Optional.empty())

        val exception =
            assertThrows<ResponseStatusException> {
                userService.deleteAccount(userId)
            }

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.statusCode
        )

        verify(
            libraryEntryRepository,
            never()
        ).deleteByUser_Id(userId)

        verify(
            identityRepository,
            never()
        ).deleteByUser_Id(userId)
    }
}