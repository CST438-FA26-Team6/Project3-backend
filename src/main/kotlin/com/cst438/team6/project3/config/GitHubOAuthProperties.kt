package com.cst438.team6.project3.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.oauth.github")
data class GitHubOAuthProperties(
    var clientId: String="",
    var clientSecret: String=""
)
