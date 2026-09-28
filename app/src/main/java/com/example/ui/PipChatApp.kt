package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.AuthState
import com.example.ui.components.HistoryDrawerContent
import com.example.ui.screens.ChartScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.PipChatTheme
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH, LOGIN, CHAT, CHART, SETTINGS
}

@Composable
fun PipChatApp(
    viewModel: PipChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

    val isDark = when (uiState.userSettings.themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val isIndonesian = uiState.userSettings.language == "id"

    // Sync screen with auth state
    LaunchedEffect(uiState.authState) {
        if (uiState.authState is AuthState.Authenticated && currentScreen == AppScreen.LOGIN) {
            currentScreen = AppScreen.CHAT
        }
    }

    // BackHandler for custom screen state
    BackHandler(enabled = drawerState.isOpen || (currentScreen != AppScreen.CHAT && currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.LOGIN)) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentScreen != AppScreen.CHAT) {
            if (currentScreen == AppScreen.CHART) {
                viewModel.clearChart()
            }
            currentScreen = AppScreen.CHAT
        }
    }

    PipChatTheme(darkTheme = isDark) {
        when (currentScreen) {
            AppScreen.SPLASH -> {
                SplashScreen(
                    onSplashFinished = {
                        currentScreen = if (uiState.authState is AuthState.Authenticated) {
                            AppScreen.CHAT
                        } else {
                            AppScreen.LOGIN
                        }
                    }
                )
            }

            AppScreen.LOGIN -> {
                LoginScreen(
                    authState = uiState.authState,
                    isIndonesian = isIndonesian,
                    onLoginGoogle = { viewModel.loginWithGoogle() },
                    onLoginApple = { viewModel.loginWithApple() },
                    onRequestOtp = { email -> viewModel.requestOtp(email) },
                    onVerifyOtp = { email, code -> viewModel.verifyOtp(email, code) },
                    onLoginPassword = { email, pass -> viewModel.loginWithPassword(email, pass) },
                    onContinueGuest = {
                        viewModel.continueAsGuest()
                        currentScreen = AppScreen.CHAT
                    }
                )
            }

            else -> {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            HistoryDrawerContent(
                                sessions = uiState.sessions,
                                currentSessionId = uiState.currentSessionId,
                                currentUser = uiState.currentUser,
                                isIndonesian = isIndonesian,
                                onSelectSession = { sessionId ->
                                    viewModel.switchSession(sessionId)
                                    scope.launch { drawerState.close() }
                                    currentScreen = AppScreen.CHAT
                                },
                                onNewChat = {
                                    viewModel.createNewChat()
                                    scope.launch { drawerState.close() }
                                    currentScreen = AppScreen.CHAT
                                },
                                onDeleteSession = { sessionId ->
                                    viewModel.deleteSession(sessionId)
                                },
                                onOpenSettings = {
                                    scope.launch { drawerState.close() }
                                    currentScreen = AppScreen.SETTINGS
                                },
                                onLogout = {
                                    scope.launch { drawerState.close() }
                                    viewModel.logout()
                                    currentScreen = AppScreen.LOGIN
                                }
                            )
                        }
                    }
                ) {
                    when (currentScreen) {
                        AppScreen.CHAT -> {
                            val activeSession = uiState.sessions.find { it.id == uiState.currentSessionId }
                            ChatScreen(
                                messages = uiState.messages,
                                isStreaming = uiState.isStreaming,
                                currentToolStatus = uiState.currentToolStatus,
                                sessionTitle = activeSession?.title ?: "",
                                settings = uiState.userSettings,
                                onSendMessage = { text -> viewModel.sendMessage(text) },
                                onStopStreaming = { viewModel.stopStreaming() },
                                onNewChat = { viewModel.createNewChat() },
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                                onViewChart = { signal ->
                                    viewModel.openChartForSignal(signal)
                                    currentScreen = AppScreen.CHART
                                }
                            )
                        }

                        AppScreen.CHART -> {
                            uiState.selectedSignalForChart?.let { signal ->
                                ChartScreen(
                                    signal = signal,
                                    candles = uiState.chartCandles,
                                    isLoading = uiState.isChartLoading,
                                    currentTimeframe = uiState.currentChartTimeframe,
                                    isIndonesian = isIndonesian,
                                    onTimeframeChanged = { tf -> viewModel.changeChartTimeframe(tf) },
                                    onBack = {
                                        viewModel.clearChart()
                                        currentScreen = AppScreen.CHAT
                                    }
                                )
                            } ?: run {
                                currentScreen = AppScreen.CHAT
                            }
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                settings = uiState.userSettings,
                                currentUser = uiState.currentUser,
                                onUpdateSettings = { newSettings -> viewModel.updateSettings(newSettings) },
                                onClearHistory = { viewModel.clearAllHistory() },
                                onLogout = {
                                    viewModel.logout()
                                    currentScreen = AppScreen.LOGIN
                                },
                                onBack = { currentScreen = AppScreen.CHAT }
                            )
                        }

                        else -> { /* handled above */ }
                    }
                }
            }
        }
    }
}
