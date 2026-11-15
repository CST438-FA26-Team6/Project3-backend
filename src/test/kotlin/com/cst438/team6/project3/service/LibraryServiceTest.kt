package com.cst438.team6.project3.service

import com.cst438.team6.project3.library.CreateLibraryEntryRequest
import com.cst438.team6.project3.library.LibraryEntry
import com.cst438.team6.project3.library.LibraryEntryRepository
import com.cst438.team6.project3.library.LibraryStatus
import com.cst438.team6.project3.title.MediaType
import com.cst438.team6.project3.title.Title
import com.cst438.team6.project3.title.TitleRepository
import com.cst438.team6.project3.user.User
import com.cst438.team6.project3.user.UserRepository
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
import kotlin.test.assertNull

class LibraryServiceTest {
    private val libraryEntryRepository =
        mock(LibraryEntryRepository::class.java)
    private val userRepository = mock(UserRepository::class.java)
    private val titleRepository = mock(TitleRepository::class.java)
    private val libraryService = LibraryService(
        libraryEntryRepository,
        userRepository,
        titleRepository
    )

    private val userId = UUID.randomUUID()
    private val user = User(
        username = "emiliano",
        email = "emiliano@example.com",
        id = userId
    )
    private val anime = Title(
        title = "Cowboy Bebop",
        mediaType = MediaType.ANIME,
        id = 7,
        total = 26
    )

    @Test
    fun `create saves an owned entry with progress zero`() {
        `when`(userRepository.findById(userId))
            .thenReturn(Optional.of(user))
        `when`(titleRepository.findById(anime.id))
            .thenReturn(Optional.of(anime))
        `when`(
            libraryEntryRepository.existsByUser_IdAndTitle_Id(
                userId,
                anime.id
            )
        ).thenReturn(false)
        `when`(libraryEntryRepository.save(any(LibraryEntry::class.java)))
            .thenAnswer { invocation ->
                invocation.getArgument<LibraryEntry>(0).apply { id = 12 }
            }

        val result = libraryService.create(
            userId,
            CreateLibraryEntryRequest(
                titleId = anime.id,
                status = LibraryStatus.WATCHING
            )
        )

        assertEquals(12, result.id)
        assertEquals(anime.id, result.titleId)
        assertEquals("Cowboy Bebop", result.title)
        assertEquals(MediaType.ANIME, result.mediaType)
        assertEquals(LibraryStatus.WATCHING, result.status)
        assertEquals(0, result.progress)
        assertEquals(26, result.total)
        assertNull(result.rating)
    }

    @Test
    fun `create rejects a token for a missing user`() {
        `when`(userRepository.findById(userId))
            .thenReturn(Optional.empty())

        val exception = assertThrows<ResponseStatusException> {
            libraryService.create(
                userId,
                CreateLibraryEntryRequest(7, LibraryStatus.WATCHING)
            )
        }

        assertEquals(HttpStatus.UNAUTHORIZED, exception.statusCode)
        verify(titleRepository, never()).findById(7)
    }

    @Test
    fun `create rejects an unknown title`() {
        `when`(userRepository.findById(userId))
            .thenReturn(Optional.of(user))
        `when`(titleRepository.findById(99))
            .thenReturn(Optional.empty())

        val exception = assertThrows<ResponseStatusException> {
            libraryService.create(
                userId,
                CreateLibraryEntryRequest(99, LibraryStatus.WATCHING)
            )
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `create rejects an anime reading status`() {
        `when`(userRepository.findById(userId))
            .thenReturn(Optional.of(user))
        `when`(titleRepository.findById(anime.id))
            .thenReturn(Optional.of(anime))

        val exception = assertThrows<ResponseStatusException> {
            libraryService.create(
                userId,
                CreateLibraryEntryRequest(anime.id, LibraryStatus.READING)
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        verify(libraryEntryRepository, never())
            .save(any(LibraryEntry::class.java))
    }

    @Test
    fun `create rejects a duplicate title for the same user`() {
        `when`(userRepository.findById(userId))
            .thenReturn(Optional.of(user))
        `when`(titleRepository.findById(anime.id))
            .thenReturn(Optional.of(anime))
        `when`(
            libraryEntryRepository.existsByUser_IdAndTitle_Id(
                userId,
                anime.id
            )
        ).thenReturn(true)

        val exception = assertThrows<ResponseStatusException> {
            libraryService.create(
                userId,
                CreateLibraryEntryRequest(anime.id, LibraryStatus.WATCHING)
            )
        }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
        verify(libraryEntryRepository, never())
            .save(any(LibraryEntry::class.java))
    }
}
