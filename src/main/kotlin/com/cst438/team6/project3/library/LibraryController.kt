package com.cst438.team6.project3.library

import com.cst438.team6.project3.service.LibraryService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/library")
class LibraryController(
    private val libraryService: LibraryService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createEntry(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateLibraryEntryRequest
    ): LibraryEntryResponse {
        val userId = try {
            UUID.fromString(jwt.subject)
        } catch (_: IllegalArgumentException) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Token subject is not a valid user ID."
            )
        }

        return libraryService.create(userId, request)
    }
}
