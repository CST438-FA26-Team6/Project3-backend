package com.cst438.team6.project3.service

import com.cst438.team6.project3.library.CreateLibraryEntryRequest
import com.cst438.team6.project3.library.LibraryEntry
import com.cst438.team6.project3.library.LibraryEntryRepository
import com.cst438.team6.project3.library.LibraryEntryResponse
import com.cst438.team6.project3.title.TitleRepository
import com.cst438.team6.project3.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class LibraryService(
    private val libraryEntryRepository: LibraryEntryRepository,
    private val userRepository: UserRepository,
    private val titleRepository: TitleRepository
) {
    @Transactional
    fun create(
        authenticatedUserId: UUID,
        request: CreateLibraryEntryRequest
    ): LibraryEntryResponse {
        val user = userRepository.findById(authenticatedUserId).orElseThrow {
            ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user no longer exists."
            )
        }
        val title = titleRepository.findById(request.titleId).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "title ${request.titleId} was not found"
            )
        }

        if (!request.status.supports(title.mediaType)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "${request.status} is not valid for ${title.mediaType}"
            )
        }

        if (
            libraryEntryRepository.existsByUser_IdAndTitle_Id(
                authenticatedUserId,
                request.titleId
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "title ${request.titleId} is already in the user's library"
            )
        }

        val entry = libraryEntryRepository.save(
            LibraryEntry(
                user = user,
                title = title,
                status = request.status,
                progress = 0
            )
        )

        return LibraryEntryResponse.from(entry)
    }
}
