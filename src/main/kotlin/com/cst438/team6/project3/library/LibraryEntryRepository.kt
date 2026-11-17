package com.cst438.team6.project3.library

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LibraryEntryRepository : JpaRepository<LibraryEntry, Long> {
    fun existsByUser_IdAndTitle_Id(
        userId: UUID,
        titleId: Long
    ): Boolean
}
