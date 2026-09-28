package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.AuthState
import com.example.model.UserProfile
import com.example.ui.components.PipChatLogoIcon
import com.example.ui.components.PipChatWordmark
import com.example.ui.theme.LocalPipCustomColors
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    authState: AuthState,
    isIndonesian: Boolean,
    onLoginGoogle: () -> Unit,
    onLoginApple: () -> Unit,
    onRequestOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit,
    onLoginPassword: (String, String) -> Unit,
    onContinueGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customColors = LocalPipCustomColors.current
    val scrollState = rememberScrollState()

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var authMode by remember { mutableStateOf("otp") } // "otp" or "password"
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resendCountdown by remember { mutableIntStateOf(0) }

    // Countdown timer for OTP resend
    LaunchedEffect(resendCountdown) {
        if (resendCountdown > 0) {
            delay(1000)
            resendCountdown -= 1
        }
    }

    // Auto-populate email if OtpSent
    LaunchedEffect(authState) {
        isSubmitting = false
        if (authState is AuthState.OtpSent) {
            emailInput = authState.email
            resendCountdown = 60
            Toast.makeText(
                context,
                if (isIndonesian) "Kode OTP terkirim: ${authState.otpCode}" else "OTP Code sent: ${authState.otpCode}",
                Toast.LENGTH_LONG
            ).show()
        } else if (authState is AuthState.Error) {
            errorMessage = authState.message
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Branding Top
            PipChatLogoIcon(
                size = 72.dp,
                animated = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            PipChatWordmark(
                iconSize = 0.dp,
                fontSize = 28.sp,
                isDark = true
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isIndonesian) "Asisten Analisa Teknikal Forex & Emas" else "AI Forex & Gold Technical Analyst",
                style = MaterialTheme.typography.labelSmall,
                color = customColors.textMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Social OAuth Section: Google & Apple
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Continue with Google Button
                Button(
                    onClick = {
                        isSubmitting = true
                        onLoginGoogle()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F1F1F)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isIndonesian) "Lanjut dengan Google" else "Continue with Google",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF1F1F1F)
                    )
                }

                // 2. Sign in with Apple Button (PRD Section 6.3 & 14)
                Button(
                    onClick = {
                        isSubmitting = true
                        onLoginApple()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("apple_login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.25f))
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_apple_logo),
                        contentDescription = "Apple",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isIndonesian) "Lanjut dengan Apple" else "Sign in with Apple",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider: or with email
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = customColors.line
                )
                Text(
                    text = if (isIndonesian) "  atau dengan email  " else "  or with email  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = customColors.textMuted
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = customColors.line
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Mode Toggle (OTP vs Password)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(customColors.surface2)
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (authMode == "otp") customColors.gold else Color.Transparent)
                        .clickable {
                            authMode = "otp"
                            errorMessage = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isIndonesian) "Kode OTP (Instan)" else "OTP Code (Passwordless)",
                        color = if (authMode == "otp") Color.Black else customColors.textMuted,
                        fontWeight = if (authMode == "otp") FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (authMode == "password") customColors.gold else Color.Transparent)
                        .clickable {
                            authMode = "password"
                            errorMessage = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isIndonesian) "Password" else "Password",
                        color = if (authMode == "password") Color.Black else customColors.textMuted,
                        fontWeight = if (authMode == "password") FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Error Banner
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(customColors.sell.copy(alpha = 0.15f))
                            .border(1.dp, customColors.sell.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = err,
                            color = customColors.sell,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Form Content based on Auth Mode
            if (authMode == "otp") {
                val isOtpSent = authState is AuthState.OtpSent

                if (!isOtpSent) {
                    // Step 1: Input Email & Request OTP
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            errorMessage = null
                        },
                        label = { Text(if (isIndonesian) "Alamat Email" else "Email Address") },
                        placeholder = { Text("trader@contoh.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = customColors.gold
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = customColors.surface2,
                            unfocusedContainerColor = customColors.surface2,
                            focusedIndicatorColor = customColors.gold,
                            unfocusedIndicatorColor = customColors.line
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (emailInput.isNotBlank()) {
                                isSubmitting = true
                                onRequestOtp(emailInput)
                            }
                        })
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (emailInput.isNotBlank()) {
                                isSubmitting = true
                                onRequestOtp(emailInput)
                            } else {
                                errorMessage = if (isIndonesian) "Silakan masukkan email" else "Please enter your email"
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("request_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = customColors.gold,
                            contentColor = Color.Black
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isIndonesian) "Kirim Kode Verifikasi OTP" else "Send OTP Verification Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    // Step 2: Input 6-Digit OTP Code
                    val otpSentState = authState as AuthState.OtpSent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(customColors.surface2)
                            .border(1.dp, customColors.gold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = customColors.gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isIndonesian) "Kode OTP 6-Digit Terkirim" else "6-Digit OTP Code Sent",
                                    fontWeight = FontWeight.Bold,
                                    color = customColors.gold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isIndonesian) "Masukkan kode yang dikirim ke ${otpSentState.email}" else "Enter the code sent to ${otpSentState.email}",
                                style = MaterialTheme.typography.labelSmall,
                                color = customColors.textMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Demo Test Helper chip
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isIndonesian) "Kode Demo Anda: " else "Your Demo Code: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = customColors.textMuted
                                )
                                Text(
                                    text = otpSentState.otpCode,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = customColors.gold,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = {
                            if (it.length <= 6) {
                                otpInput = it
                                errorMessage = null
                                if (it.length == 6) {
                                    onVerifyOtp(otpSentState.email, it)
                                }
                            }
                        },
                        label = { Text(if (isIndonesian) "Kode OTP (6 Angka)" else "6-Digit OTP Code") },
                        placeholder = { Text("123456") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Password,
                                contentDescription = "OTP",
                                tint = customColors.gold
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = customColors.surface2,
                            unfocusedContainerColor = customColors.surface2,
                            focusedIndicatorColor = customColors.gold,
                            unfocusedIndicatorColor = customColors.line
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            textAlign = TextAlign.Center,
                            letterSpacing = 6.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (otpInput.length == 6) {
                                isSubmitting = true
                                onVerifyOtp(otpSentState.email, otpInput)
                            } else {
                                errorMessage = if (isIndonesian) "Masukkan 6 digit kode OTP" else "Please enter 6-digit OTP code"
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("verify_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = customColors.gold,
                            contentColor = Color.Black
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isIndonesian) "Verifikasi & Masuk" else "Verify & Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Resend OTP / Change Email
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (resendCountdown == 0) {
                                    onRequestOtp(otpSentState.email)
                                }
                            },
                            enabled = resendCountdown == 0
                        ) {
                            Text(
                                text = if (resendCountdown > 0) {
                                    if (isIndonesian) "Kirim ulang (${resendCountdown}s)" else "Resend (${resendCountdown}s)"
                                } else {
                                    if (isIndonesian) "Kirim ulang OTP" else "Resend OTP"
                                },
                                color = if (resendCountdown == 0) customColors.gold else customColors.textMuted,
                                fontSize = 12.sp
                            )
                        }

                        TextButton(
                            onClick = {
                                onRequestOtp("") // reset OTP state
                            }
                        ) {
                            Text(
                                text = if (isIndonesian) "Ganti email" else "Change email",
                                color = customColors.textMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                // Password Mode Form
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = null
                    },
                    label = { Text(if (isIndonesian) "Alamat Email" else "Email Address") },
                    placeholder = { Text("trader@contoh.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = customColors.gold
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_password_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = customColors.surface2,
                        unfocusedContainerColor = customColors.surface2,
                        focusedIndicatorColor = customColors.gold,
                        unfocusedIndicatorColor = customColors.line
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text(if (isIndonesian) "Kata Sandi" else "Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = customColors.gold
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = customColors.textMuted
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = customColors.surface2,
                        unfocusedContainerColor = customColors.surface2,
                        focusedIndicatorColor = customColors.gold,
                        unfocusedIndicatorColor = customColors.line
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                            isSubmitting = true
                            onLoginPassword(emailInput, passwordInput)
                        }
                    })
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                            isSubmitting = true
                            onLoginPassword(emailInput, passwordInput)
                        } else {
                            errorMessage = if (isIndonesian) "Harap isi email dan kata sandi" else "Please enter email and password"
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("password_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.gold,
                        contentColor = Color.Black
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isIndonesian) "Masuk / Daftar" else "Sign In / Register",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Demo / Guest Trader Mode
            OutlinedButton(
                onClick = onContinueGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("guest_login_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(customColors.line)
                )
            ) {
                Text(
                    text = if (isIndonesian) "Lanjut sebagai Akun Demo (Tamu)" else "Continue as Demo Trader",
                    style = MaterialTheme.typography.bodyMedium,
                    color = customColors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Mandatory Compliance Disclaimer
            Text(
                text = if (isIndonesian) "Bukan saran keuangan. Trading berisiko kehilangan modal." else "Not financial advice. Trading involves risk of capital loss.",
                style = MaterialTheme.typography.labelSmall,
                color = customColors.textMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
