package com.cst438.team6.project3.user

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IdentityRepository : JpaRepository<Identity, UUID> {

    fun findByProviderAndProviderUserId(
        provider: AuthProvider,
        providerUserId: String
    ): Identity?

    fun findByUser_Id(userId: UUID): List<Identity>
}