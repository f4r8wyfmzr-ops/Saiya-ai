package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.SiyaTheme
import com.example.viewmodel.SiyaViewModel

object SiyaRoutes {
    const val SPLASH = "splash"
    const val CHAT = "chat"
    const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            SiyaTheme {
                SiyaApp()
            }
        }
    }
}

@Composable
fun SiyaApp(
    viewModel: SiyaViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = SiyaRoutes.SPLASH
    ) {
        composable(SiyaRoutes.SPLASH) {
            SplashScreen(
                ownerName = uiState.preferences.ownerName,
                onFinished = {
                    navController.navigate(SiyaRoutes.CHAT) {
                        popUpTo(SiyaRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(SiyaRoutes.CHAT) {
            ChatScreen(
                uiState = uiState,
                onSendMessage = viewModel::sendUserMessage,
                onStartVoice = viewModel::startVoiceListening,
                onStopVoice = viewModel::stopVoiceListening,
                onClearVoiceError = viewModel::clearVoiceError,
                onSpeakMessage = viewModel::speakMessage,
                onSpeakGreeting = viewModel::speakGreeting,
                onStopSpeaking = viewModel::stopSpeaking,
                onCycleLanguage = viewModel::cycleLanguage,
                onConfirmAction = viewModel::confirmPendingAction,
                onDismissAction = viewModel::dismissPendingAction,
                onOpenSettings = { navController.navigate(SiyaRoutes.SETTINGS) },
                onRefreshTelemetry = viewModel::refreshTelemetryAndPermissions,
                onDismissFirebaseNotice = viewModel::dismissFirebaseNotice,
                onClearSnackbar = viewModel::clearSnackbar,
                onNotifyPermissionDenied = viewModel::notifyMessage
            )
        }

        composable(SiyaRoutes.SETTINGS) {
            SettingsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onSaveOwnerName = viewModel::updateOwnerName,
                onSelectLanguage = viewModel::updateLanguage,
                onToggleAutoSpeak = viewModel::updateAutoSpeak,
                onChangeSpeechRate = viewModel::updateSpeechRate,
                onChangeSpeechPitch = viewModel::updateSpeechPitch,
                onSelectVoice = viewModel::updateSelectedVoice,
                onTestVoice = viewModel::speakGreeting,
                onToggleSaveHistory = viewModel::updateSaveChatHistory,
                onClearHistory = viewModel::clearChatHistory,
                onDeleteReminder = viewModel::deleteReminder,
                onResetPreferences = viewModel::resetPreferences,
                onOpenAppSettings = viewModel::openAppSettings,
                onRefreshTelemetry = viewModel::refreshTelemetryAndPermissions
            )
        }
    }
}
