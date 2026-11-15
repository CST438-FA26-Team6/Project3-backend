package com.cst438.team6.project3.library

import com.cst438.team6.project3.title.MediaType

enum class LibraryStatus {
    WATCHING,
    READING,
    COMPLETED,
    ON_HOLD,
    DROPPED,
    PLAN_TO_WATCH,
    PLAN_TO_READ;

    fun supports(mediaType: MediaType): Boolean {
        return when (mediaType) {
            MediaType.ANIME -> this != READING && this != PLAN_TO_READ
            MediaType.MANGA -> this != WATCHING && this != PLAN_TO_WATCH
        }
    }
}
