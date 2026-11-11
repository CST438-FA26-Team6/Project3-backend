package com.cst438.team6.project3.auth

import com.cst438.team6.project3.user.AuthProvider

data class ProviderIdentity(
    val provider: AuthProvider,
    val providerUserId: String,
    val username: String,
    val email: String,
    val emailVerified: Boolean
)