package com.cst438.team6.project3.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun animeMangaTrackerOpenApi(): OpenAPI {
        return OpenAPI().info(
            Info()
                .title("Anime and Manga Tracker API")
                .version("0.3.0")
                .description(
                    "API for authentication, title search, " +
                        "personal libraries, and user profiles."
                )
        )
    }
}
