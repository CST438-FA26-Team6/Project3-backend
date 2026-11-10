package com.cst438.team6.project3.auth

import com.cst438.team6.project3.user.User
import com.cst438.team6.project3.user.UserRole
import java.util.UUID

data class CodeExchangeRequest(
    val code: String,
    val codeVerifier: String,
    val redirectUri: String
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val role: UserRole
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = requireNotNull(user.id),
                username = user.username,
                email = user.email,
                role = user.role
            )
        }
    }
}