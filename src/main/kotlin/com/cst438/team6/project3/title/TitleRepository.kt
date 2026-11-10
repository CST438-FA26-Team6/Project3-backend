package com.cst438.team6.project3.title

import org.springframework.data.jpa.repository.JpaRepository

interface TitleRepository : JpaRepository<Title, Long> {
    fun findByTitleContainingIgnoreCaseOrderByTitleAsc(
        title: String
    ): List<Title>

    fun findByTitleContainingIgnoreCaseAndMediaTypeOrderByTitleAsc(
        title: String,
        mediaType: MediaType
    ): List<Title>
}
