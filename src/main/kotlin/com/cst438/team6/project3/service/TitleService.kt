package com.cst438.team6.project3.service

import com.cst438.team6.project3.title.MediaType
import com.cst438.team6.project3.title.Title
import com.cst438.team6.project3.title.TitleRepository
import com.cst438.team6.project3.title.TitleResponse
import com.cst438.team6.project3.title.UpdateTitleRequest
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
        return TitleResponse.from(findTitle(titleId))
    }

    fun update(
        titleId: Long,
        request: UpdateTitleRequest
    ): TitleResponse {
        val normalizedTitle = request.title.trim()
        if (normalizedTitle.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "title must not be blank"
            )
        }
        if (request.total != null && request.total < 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "total must not be negative"
            )
        }

        val title = findTitle(titleId)
        title.title = normalizedTitle
        title.mediaType = request.mediaType
        title.total = request.total

        return TitleResponse.from(titleRepository.save(title))
    }

    fun delete(titleId: Long) {
        titleRepository.delete(findTitle(titleId))
    }

    private fun findTitle(titleId: Long): Title {
        return titleRepository.findById(titleId).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "title $titleId was not found"
            )
        }
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
