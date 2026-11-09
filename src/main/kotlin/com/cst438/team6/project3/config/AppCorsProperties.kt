package com.cst438.team6.project3.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.cors")
data class AppCorsProperties(
    var allowedOrigins: List<String> = listOf("http://localhost:8081")
)
