package com.cst438.team6.project3.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleTokenResponse(
    @JsonProperty("id_token")
    val idToken: String
)

data class GoogleAuthRequest(
    val idToken: String? = null,
    val code: String? = null,
    val codeVerifier: String? = null,
    val redirectUri: String? = null
)