package com.cst438.team6.project3.title

data class UpdateTitleRequest(
    val title: String,
    val mediaType: MediaType,
    val total: Int?
)
