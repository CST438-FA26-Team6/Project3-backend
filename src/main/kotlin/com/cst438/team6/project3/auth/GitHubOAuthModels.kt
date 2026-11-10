package com.cst438.team6.project3.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String
)

data class GitHubUserResponse(
    val id: Long,
    val login: String,
    val name: String? = null
)

data class GitHubEmailResponse(
    val email: String,
    val primary: Boolean,
    val verified: Boolean
)