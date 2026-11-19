package com.cst438.team6.project3.service

import com.cst438.team6.project3.library.LibraryEntryRepository
import com.cst438.team6.project3.user.IdentityRepository
import com.cst438.team6.project3.user.UpdateUserProfileRequest
import com.cst438.team6.project3.user.UserProfileResponse
import com.cst438.team6.project3.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val identityRepository: IdentityRepository,
    private val libraryEntryRepository: LibraryEntryRepository
) {

    @Transactional(readOnly = true)
    fun getProfile(userId: UUID): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found."
                )
            }

        return UserProfileResponse.from(user)
    }

    @Transactional
    fun updateProfile(
        userId: UUID,
        request: UpdateUserProfileRequest
    ): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found."
                )
            }

        val username = request.username.trim()

        if (username.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Username cannot be blank."
            )
        }

        user.username = username

        val savedUser = userRepository.save(user)

        return UserProfileResponse.from(savedUser)
    }

    @Transactional
    fun deleteAccount(userId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found."
                )
            }

        libraryEntryRepository.deleteByUser_Id(userId)
        identityRepository.deleteByUser_Id(userId)
        userRepository.delete(user)
    }
}