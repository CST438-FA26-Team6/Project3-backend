package com.cst438.team6.project3.title

data class TitleResponse(
    val id: Long,
    val title: String,
    val mediaType: MediaType
) {
    companion object {
        fun from(title: Title): TitleResponse {
            return TitleResponse(
                id = title.id,
                title = title.title,
                mediaType = title.mediaType
            )
        }
    }
}
