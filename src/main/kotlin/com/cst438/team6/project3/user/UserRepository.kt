package com.cst438.team6.project3.user

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {

    fun findByEmailIgnoreCase(email: String): User?
}