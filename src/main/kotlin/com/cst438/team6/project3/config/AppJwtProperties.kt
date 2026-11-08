package com.cst438.team6.project3.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.jwt")
data class AppJwtProperties(
    var issuer: String = "",
    var secretBase64: String = "",
    var ttl: Duration = Duration.ofHours(1)
)

