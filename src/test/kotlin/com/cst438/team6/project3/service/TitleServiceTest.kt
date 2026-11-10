package com.cst438.team6.project3.service

import com.cst438.team6.project3.title.MediaType
import com.cst438.team6.project3.title.Title
import com.cst438.team6.project3.title.TitleRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import kotlin.test.assertEquals

class TitleServiceTest {
    private val titleRepository = mock(TitleRepository::class.java)
    private val titleService = TitleService(titleRepository)

    @Test
    fun `search returns matching titles without a media filter`() {
        val titles = listOf(
            Title("Cowboy Bebop", MediaType.ANIME, 1),
            Title("Cowboy Bebop Shooting Star", MediaType.MANGA, 2)
        )
        `when`(
            titleRepository
                .findByTitleContainingIgnoreCaseOrderByTitleAsc("Cowboy")
        ).thenReturn(titles)

        val result = titleService.search("  Cowboy  ", null)

        assertEquals(2, result.size)
        assertEquals("Cowboy Bebop", result[0].title)
        assertEquals(MediaType.ANIME, result[0].mediaType)
    }

    @Test
    fun `search accepts a case-insensitive media filter`() {
        val titles = listOf(
            Title("Cowboy Bebop Shooting Star", MediaType.MANGA, 2)
        )
        `when`(
            titleRepository
                .findByTitleContainingIgnoreCaseAndMediaTypeOrderByTitleAsc(
                    "Cowboy",
                    MediaType.MANGA
                )
        ).thenReturn(titles)

        val result = titleService.search("Cowboy", "manga")

        assertEquals(1, result.size)
        assertEquals(MediaType.MANGA, result.single().mediaType)
    }

    @Test
    fun `blank query returns bad request`() {
        val exception = assertThrows<ResponseStatusException> {
            titleService.search("   ", null)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        verifyNoInteractions(titleRepository)
    }

    @Test
    fun `invalid media type returns bad request`() {
        val exception = assertThrows<ResponseStatusException> {
            titleService.search("Cowboy", "book")
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        verifyNoInteractions(titleRepository)
    }

    @Test
    fun `find by id returns the matching title`() {
        val title = Title("Cowboy Bebop", MediaType.ANIME, 1)
        `when`(titleRepository.findById(1)).thenReturn(Optional.of(title))

        val result = titleService.findById(1)

        assertEquals(1, result.id)
        assertEquals("Cowboy Bebop", result.title)
        verify(titleRepository).findById(1)
    }

    @Test
    fun `find by id returns not found for an unknown title`() {
        `when`(titleRepository.findById(99)).thenReturn(Optional.empty())

        val exception = assertThrows<ResponseStatusException> {
            titleService.findById(99)
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }
}
