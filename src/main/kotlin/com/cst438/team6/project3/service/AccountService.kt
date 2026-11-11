package com.cst438.team6.project3.service

import com.cst438.team6.project3.auth.ProviderIdentity
import com.cst438.team6.project3.user.Identity
import com.cst438.team6.project3.user.IdentityRepository
import com.cst438.team6.project3.user.User
import com.cst438.team6.project3.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AccountService(
    private val userRepository: UserRepository,
    private val identityRepository: IdentityRepository
) {

    @Transactional
    fun findOrCreate(identity: ProviderIdentity): User {
        val existingIdentity =
            identityRepository.findByProviderAndProviderUserId(
                identity.provider,
                identity.providerUserId
            )

        if (existingIdentity != null) {
            return existingIdentity.user
        }

        if (!identity.emailVerified) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Verify your email with ${identity.provider.name.lowercase()}."
            )
        }

        val normalizedEmail = identity.email.trim().lowercase()
        val existingUser =
            userRepository.findByEmailIgnoreCase(normalizedEmail)

        if (existingUser != null) {
            val existingProvider = existingUser.id?.let { userId ->
                identityRepository.findByUser_Id(userId)
                    .firstOrNull()
                    ?.provider
                    ?.name
                    ?.lowercase()
            } ?: "another provider"

            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "This email is already registered with $existingProvider."
            )
        }

        val user = userRepository.save(
            User(
                username = identity.username.trim(),
                email = normalizedEmail
            )
        )

        identityRepository.save(
            Identity(
                user = user,
                provider = identity.provider,
                providerUserId = identity.providerUserId
            )
        )

        return user
    }
}