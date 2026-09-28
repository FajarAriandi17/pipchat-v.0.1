package com.example.model

enum class AuthProvider {
    GOOGLE, APPLE, EMAIL_OTP, GUEST
}

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val provider: AuthProvider,
    val photoUrl: String? = null,
    val isVerified: Boolean = true
)
