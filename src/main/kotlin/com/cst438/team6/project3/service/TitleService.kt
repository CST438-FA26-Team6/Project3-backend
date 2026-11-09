package com.cst438.team6.project3.service

import com.cst438.team6.project3.title.MediaType
import com.cst438.team6.project3.title.TitleRepository
import com.cst438.team6.project3.title.TitleResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class TitleService(
    private val titleRepository: TitleRepository
) {
    fun search(query: String, type: String?): List<TitleResponse> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "query must not be blank"
            )
        }

        val mediaType = parseMediaType(type)
        val titles = if (mediaType == null) {
            titleRepository
                .findByTitleContainingIgnoreCaseOrderByTitleAsc(
                    normalizedQuery
                )
        } else {
            titleRepository
                .findByTitleContainingIgnoreCaseAndMediaTypeOrderByTitleAsc(
                    normalizedQuery,
                    mediaType
                )
        }

        return titles.map(TitleResponse::from)
    }

    fun findById(titleId: Long): TitleResponse {
        val title = titleRepository.findById(titleId).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "title $titleId was not found"
            )
        }

        return TitleResponse.from(title)
    }

    private fun parseMediaType(type: String?): MediaType? {
        if (type.isNullOrBlank()) {
            return null
        }

        return try {
            MediaType.valueOf(type.trim().uppercase())
        } catch (_: IllegalArgumentException) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "type must be ANIME or MANGA"
            )
        }
    }
}
