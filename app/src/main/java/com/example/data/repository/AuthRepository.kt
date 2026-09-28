package com.example.data.repository

import android.content.Context
import com.example.model.AuthProvider
import com.example.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.UUID

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class OtpSent(val email: String, val otpCode: String, val expiresAt: Long) : AuthState()
    data class Authenticated(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthRepository(context: Context) {
    private val prefs = context.getSharedPreferences("pipchat_auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        val uid = prefs.getString("uid", null)
        val name = prefs.getString("name", null)
        val email = prefs.getString("email", null)
        val providerStr = prefs.getString("provider", null)

        if (uid != null && name != null && email != null) {
            val provider = try {
                AuthProvider.valueOf(providerStr ?: "GUEST")
            } catch (_: Exception) {
                AuthProvider.GUEST
            }
            _authState.value = AuthState.Authenticated(
                UserProfile(
                    uid = uid,
                    displayName = name,
                    email = email,
                    provider = provider
                )
            )
        }
    }

    private fun saveSession(user: UserProfile) {
        prefs.edit()
            .putString("uid", user.uid)
            .putString("name", user.displayName)
            .putString("email", user.email)
            .putString("provider", user.provider.name)
            .apply()
        _authState.value = AuthState.Authenticated(user)
    }

    suspend fun signInWithGoogle(): Result<UserProfile> = withContext(Dispatchers.IO) {
        delay(600) // Simulate OAuth handshake
        val user = UserProfile(
            uid = "g_${UUID.randomUUID().toString().take(12)}",
            displayName = "Trader Google",
            email = "trader@gmail.com",
            provider = AuthProvider.GOOGLE
        )
        saveSession(user)
        Result.success(user)
    }

    suspend fun signInWithApple(): Result<UserProfile> = withContext(Dispatchers.IO) {
        delay(600) // Simulate Apple OAuth with nonce
        // Handle Apple Relay / Hide My Email feature (PRD Section 17)
        val user = UserProfile(
            uid = "apple_${UUID.randomUUID().toString().take(12)}",
            displayName = "Apple Trader",
            email = "trader.apple@privaterelay.appleid.com",
            provider = AuthProvider.APPLE
        )
        saveSession(user)
        Result.success(user)
    }

    suspend fun requestOtp(email: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmed = email.trim().lowercase()
        if (trimmed.isEmpty() || !trimmed.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Email tidak valid"))
        }

        delay(500)
        // Generate 6-digit numeric OTP code
        val random = SecureRandom()
        val otpCode = String.format("%06d", random.nextInt(1000000))
        val expiresAt = System.currentTimeMillis() + (5 * 60 * 1000L) // 5 minutes

        _authState.value = AuthState.OtpSent(
            email = trimmed,
            otpCode = otpCode,
            expiresAt = expiresAt
        )
        Result.success(otpCode)
    }

    suspend fun verifyOtp(email: String, enteredOtp: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        delay(400)
        val currentState = _authState.value
        val valid = if (currentState is AuthState.OtpSent) {
            currentState.email == email && (currentState.otpCode == enteredOtp || enteredOtp == "123456")
        } else {
            enteredOtp == "123456" // demo bypass
        }

        if (valid) {
            val user = UserProfile(
                uid = "otp_${UUID.randomUUID().toString().take(12)}",
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email,
                provider = AuthProvider.EMAIL_OTP
            )
            saveSession(user)
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Kode OTP salah atau telah kadaluarsa"))
        }
    }

    suspend fun signInWithEmailPassword(email: String, pass: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val trimmed = email.trim().lowercase()
        if (!trimmed.contains("@") || pass.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Format email atau password tidak valid (min. 6 karakter)"))
        }
        delay(500)
        val user = UserProfile(
            uid = "usr_${UUID.randomUUID().toString().take(12)}",
            displayName = trimmed.substringBefore("@").replaceFirstChar { it.uppercase() },
            email = trimmed,
            provider = AuthProvider.EMAIL_OTP
        )
        saveSession(user)
        Result.success(user)
    }

    fun continueAsGuest(): UserProfile {
        val user = UserProfile(
            uid = "guest_${UUID.randomUUID().toString().take(8)}",
            displayName = "Demo Trader",
            email = "demo.trader@pipchat.io",
            provider = AuthProvider.GUEST
        )
        saveSession(user)
        return user
    }

    fun logout() {
        prefs.edit().clear().apply()
        _authState.value = AuthState.Unauthenticated
    }

    fun getCurrentUser(): UserProfile? {
        val state = _authState.value
        return if (state is AuthState.Authenticated) state.user else null
    }
}
