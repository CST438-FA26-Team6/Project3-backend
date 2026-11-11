package com.cst438.team6.project3.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.oauth.google")
data class GoogleOAuthProperties(
    var webClientId: String = "",
    var webClientSecret: String = ""
)