package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuthProvider
import com.example.model.UserProfile
import com.example.model.UserSettings
import com.example.ui.components.PipChatLogoIcon
import com.example.ui.components.PipChatWordmark
import com.example.ui.theme.LocalPipCustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    currentUser: UserProfile?,
    onUpdateSettings: (UserSettings) -> Unit,
    onClearHistory: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = LocalPipCustomColors.current
    val isIndonesian = settings.language == "id"
    val scrollState = rememberScrollState()

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isIndonesian) "Pengaturan" else "Settings",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Account Profile Card
            val providerName = when (currentUser?.provider) {
                AuthProvider.APPLE -> "Sign in with Apple"
                AuthProvider.GOOGLE -> "Google Account"
                AuthProvider.EMAIL_OTP -> "Email OTP"
                else -> "Demo Trader"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(customColors.surface2)
                    .border(1.dp, customColors.line, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.displayName ?: "Demo Trader",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(customColors.gold.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = providerName,
                                    color = customColors.gold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentUser?.email ?: "demo@pipchat.io",
                            style = MaterialTheme.typography.labelSmall,
                            color = customColors.textMuted
                        )
                    }

                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = customColors.sell
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(customColors.sell.copy(alpha = 0.5f))
                        )
                    ) {
                        Text(
                            text = if (isIndonesian) "Keluar" else "Sign Out",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Language Selection
            SettingsSection(
                title = if (isIndonesian) "Bahasa" else "Language",
                icon = Icons.Default.Language
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SegmentOption(
                        label = "Bahasa Indonesia",
                        isSelected = settings.language == "id",
                        modifier = Modifier.weight(1f)
                    ) {
                        onUpdateSettings(settings.copy(language = "id"))
                    }

                    SegmentOption(
                        label = "English",
                        isSelected = settings.language == "en",
                        modifier = Modifier.weight(1f)
                    ) {
                        onUpdateSettings(settings.copy(language = "en"))
                    }
                }
            }

            // Theme Selection
            SettingsSection(
                title = if (isIndonesian) "Tema Tampilan" else "Theme",
                icon = Icons.Default.Palette
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SegmentOption(
                        label = if (isIndonesian) "Gelap (Dark)" else "Dark",
                        isSelected = settings.themeMode == "dark",
                        modifier = Modifier.weight(1f)
                    ) {
                        onUpdateSettings(settings.copy(themeMode = "dark"))
                    }

                    SegmentOption(
                        label = if (isIndonesian) "Terang (Light)" else "Light",
                        isSelected = settings.themeMode == "light",
                        modifier = Modifier.weight(1f)
                    ) {
                        onUpdateSettings(settings.copy(themeMode = "light"))
                    }
                }
            }

            // Trading Style
            SettingsSection(
                title = if (isIndonesian) "Gaya Trading (Style)" else "Trading Style",
                icon = Icons.Default.ShowChart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "scalping" to if (isIndonesian) "Scalping" else "Scalping",
                        "day" to if (isIndonesian) "Day Trade" else "Day Trade",
                        "swing" to if (isIndonesian) "Swing" else "Swing"
                    ).forEach { (key, label) ->
                        SegmentOption(
                            label = label,
                            isSelected = settings.tradingStyle == key,
                            modifier = Modifier.weight(1f)
                        ) {
                            onUpdateSettings(settings.copy(tradingStyle = key))
                        }
                    }
                }
            }

            // Risk Per Trade
            SettingsSection(
                title = if (isIndonesian) "Risiko per Posisi (% Akun)" else "Risk Per Trade (% Account)",
                icon = Icons.Default.Security
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1.0, 1.5, 2.0, 3.0).forEach { r ->
                        SegmentOption(
                            label = "$r%",
                            isSelected = settings.riskPercent == r,
                            modifier = Modifier.weight(1f)
                        ) {
                            onUpdateSettings(settings.copy(riskPercent = r))
                        }
                    }
                }
            }

            // Clear Data & History
            SettingsSection(
                title = if (isIndonesian) "Data & Privasi" else "Data & Privacy",
                icon = Icons.Default.Delete
            ) {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("clear_history_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = customColors.sell
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(customColors.sell.copy(alpha = 0.5f))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isIndonesian) "Hapus Semua Riwayat Percakapan" else "Clear All Chat History",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Legal & Disclaimer Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(customColors.surface2.copy(alpha = 0.6f))
                    .border(1.dp, customColors.line, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                PipChatWordmark(
                    iconSize = 28.dp,
                    fontSize = 20.sp,
                    isDark = settings.themeMode == "dark"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = customColors.gold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isIndonesian) "Pemberitahuan Risiko & Legalitas" else "Risk Disclosure & Legal",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isIndonesian) {
                        "PipChat adalah asisten analisa teknikal berbasis kecerdasan buatan (AI). Semua analisis, level harga, Stop Loss, dan Take Profit hanya disediakan untuk tujuan edukasi dan referensi ide trading semata. Aplikasi ini tidak memberikan saran investasi, tidak menjamin keuntungan finansial, dan tidak bertanggung jawab atas kerugian finansial yang timbul dari keputusan trading."
                    } else {
                        "PipChat is an AI-powered technical analysis assistant. All market analyses, price levels, Stop Loss, and Take Profit projections are generated for educational and informational purposes only. PipChat does not provide financial or investment advice, does not guarantee profitability, and assumes no liability for trading decisions."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = customColors.textMuted,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "PipChat v1.0.0 · AI Studio & Cloud Run",
                    style = MaterialTheme.typography.labelSmall,
                    color = customColors.textMuted.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(text = if (isIndonesian) "Hapus Riwayat?" else "Clear History?")
            },
            text = {
                Text(
                    text = if (isIndonesian) "Semua percakapan dan sinyal tersimpan akan dihapus secara permanen." else "All stored conversations and saved signals will be permanently deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearHistory()
                    }
                ) {
                    Text(
                        text = if (isIndonesian) "Hapus" else "Delete",
                        color = customColors.sell
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(text = if (isIndonesian) "Batal" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val customColors = LocalPipCustomColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = customColors.gold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        content()
    }
}

@Composable
private fun SegmentOption(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val customColors = LocalPipCustomColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) customColors.gold else customColors.surface2)
            .border(
                1.dp,
                if (isSelected) customColors.gold else customColors.line,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
