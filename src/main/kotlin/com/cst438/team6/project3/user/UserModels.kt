package com.cst438.team6.project3.user

import java.util.UUID

data class UserProfileResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val role: UserRole
) {
    companion object {
        fun from(user: User): UserProfileResponse {
            return UserProfileResponse(
                id = requireNotNull(user.id),
                username = user.username,
                email = user.email,
                role = user.role
            )
        }
    }
}

data class UpdateUserProfileRequest(
    val username: String
)