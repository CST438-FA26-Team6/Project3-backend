package com.cst438.team6.project3.user

import com.cst438.team6.project3.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    fun getProfile(
        @AuthenticationPrincipal jwt: Jwt
    ): UserProfileResponse {
        return userService.getProfile(
            getUserId(jwt)
        )
    }

    @PatchMapping("/me")
    fun updateProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UpdateUserProfileRequest
    ): UserProfileResponse {
        return userService.updateProfile(
            getUserId(jwt),
            request
        )
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAccount(
        @AuthenticationPrincipal jwt: Jwt
    ) {
        userService.deleteAccount(
            getUserId(jwt)
        )
    }

    private fun getUserId(jwt: Jwt): UUID {
        return try {
            UUID.fromString(jwt.subject)
        } catch (_: IllegalArgumentException) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Token subject is not a valid user ID."
            )
        }
    }
}