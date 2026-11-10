package com.cst438.team6.project3.title

import com.cst438.team6.project3.service.TitleService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/titles")
class TitleController(
    private val titleService: TitleService
) {
    @GetMapping
    fun searchTitles(
        @RequestParam query: String,
        @RequestParam(required = false) type: String?
    ): List<TitleResponse> {
        return titleService.search(query, type)
    }

    @GetMapping("/{titleId}")
    fun getTitle(
        @PathVariable titleId: Long
    ): TitleResponse {
        return titleService.findById(titleId)
    }
}
