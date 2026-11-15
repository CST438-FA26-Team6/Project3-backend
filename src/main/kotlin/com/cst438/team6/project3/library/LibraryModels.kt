package com.cst438.team6.project3.library

import com.cst438.team6.project3.title.MediaType

data class CreateLibraryEntryRequest(
    val titleId: Long,
    val status: LibraryStatus
)

data class LibraryEntryResponse(
    val id: Long,
    val titleId: Long,
    val title: String,
    val mediaType: MediaType,
    val status: LibraryStatus,
    val progress: Int,
    val total: Int?,
    val rating: Int?
) {
    companion object {
        fun from(entry: LibraryEntry): LibraryEntryResponse {
            return LibraryEntryResponse(
                id = entry.id,
                titleId = entry.title.id,
                title = entry.title.title,
                mediaType = entry.title.mediaType,
                status = entry.status,
                progress = entry.progress,
                total = entry.title.total,
                rating = entry.rating
            )
        }
    }
}
