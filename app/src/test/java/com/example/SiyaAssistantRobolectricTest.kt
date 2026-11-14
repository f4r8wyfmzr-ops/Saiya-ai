package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.PendingAssistantAction
import com.example.data.ai.SampleAiRepository
import com.example.data.local.ChatMessageEntity
import com.example.data.local.SiyaLanguage
import com.example.data.local.UserPreferences
import com.example.system.AppLauncherHelper
import com.example.system.BatteryTelemetry
import com.example.system.DeviceInfoHelper
import com.example.system.SystemActionHelper
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.SiyaTheme
import com.example.viewmodel.SiyaUiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SiyaAssistantRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `splash screen renders SIYA AI branding and owner greeting`() {
        var finished = false
        composeTestRule.setContent {
            SiyaTheme {
                SplashScreen(
                    ownerName = "Vishal Boss",
                    onFinished = { finished = true }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("splash_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("SIYA AI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ready for Vishal Boss").assertIsDisplayed()
        composeTestRule.onNodeWithTag("splash_screen").performClick()
        assertTrue(finished)
    }

    @Test
    fun `chat screen displays greeting, Firebase notice, and allows sending text message`() {
        var sentPrompt = ""
        var settingsOpened = false
        composeTestRule.setContent {
            SiyaTheme {
                ChatScreen(
                    uiState = SiyaUiState(
                        preferences = UserPreferences(ownerName = "Vishal Boss"),
                        battery = BatteryTelemetry(84, false, "On Battery"),
                        currentTimeText = "08:30 PM",
                        showFirebaseNotice = true
                    ),
                    onSendMessage = { sentPrompt = it },
                    onStartVoice = {},
                    onStopVoice = {},
                    onClearVoiceError = {},
                    onSpeakMessage = {},
                    onSpeakGreeting = {},
                    onStopSpeaking = {},
                    onCycleLanguage = {},
                    onConfirmAction = {},
                    onDismissAction = {},
                    onOpenSettings = { settingsOpened = true },
                    onRefreshTelemetry = {},
                    onDismissFirebaseNotice = {},
                    onClearSnackbar = {},
                    onNotifyPermissionDenied = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("firebase_notice_banner").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Hello Vishal Boss! Siya is ready. Bataiye aaj kya karna hai?")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("chat_input").performTextInput("Check battery")
        composeTestRule.onNodeWithTag("send_button").performClick()
        assertEquals("Check battery", sentPrompt)

        composeTestRule.onNodeWithTag("settings_button").performClick()
        assertTrue(settingsOpened)
    }

    @Test
    fun `chat screen shows confirmation dialog for pending action`() {
        var confirmed = false
        composeTestRule.setContent {
            SiyaTheme {
                ChatScreen(
                    uiState = SiyaUiState(
                        messages = listOf(
                            ChatMessageEntity(id = 1, text = "Set alarm 7 AM", isFromUser = true),
                            ChatMessageEntity(id = 2, text = "Alarm ready", isFromUser = false)
                        ),
                        pendingAction = PendingAssistantAction.SetAlarm(7, 0, "Morning")
                    ),
                    onSendMessage = {},
                    onStartVoice = {},
                    onStopVoice = {},
                    onClearVoiceError = {},
                    onSpeakMessage = {},
                    onSpeakGreeting = {},
                    onStopSpeaking = {},
                    onCycleLanguage = {},
                    onConfirmAction = { confirmed = true },
                    onDismissAction = {},
                    onOpenSettings = {},
                    onRefreshTelemetry = {},
                    onDismissFirebaseNotice = {},
                    onClearSnackbar = {},
                    onNotifyPermissionDenied = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("action_confirmation_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirm_action_button").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun `settings screen updates owner name and navigates back`() {
        var savedName = ""
        var backedOut = false
        composeTestRule.setContent {
            SiyaTheme {
                SettingsScreen(
                    uiState = SiyaUiState(),
                    onBack = { backedOut = true },
                    onSaveOwnerName = { savedName = it },
                    onSelectLanguage = {},
                    onToggleAutoSpeak = {},
                    onChangeSpeechRate = {},
                    onChangeSpeechPitch = {},
                    onSelectVoice = {},
                    onTestVoice = {},
                    onToggleSaveHistory = {},
                    onClearHistory = {},
                    onDeleteReminder = {},
                    onResetPreferences = {},
                    onOpenAppSettings = {},
                    onRefreshTelemetry = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("save_owner_name_button").performClick()
        assertEquals("Vishal Boss", savedName)

        composeTestRule.onNodeWithTag("back_button").performClick()
        assertTrue(backedOut)
    }

    @Test
    fun `sample ai repository handles battery, time, alarm, privacy guard, and labeled fallback`() = runTest {
        val repo = SampleAiRepository(
            deviceInfoHelper = DeviceInfoHelper(context),
            appLauncherHelper = AppLauncherHelper(context)
        )

        val timeReply = repo.processPrompt("What time is it?", "Vishal Boss", SiyaLanguage.HINGLISH)
        assertFalse(timeReply.isSampleFallback)
        assertTrue(timeReply.text.contains("Vishal Boss"))

        val alarmReply = repo.processPrompt("Set alarm for 6:30 am", "Vishal Boss", SiyaLanguage.ENGLISH)
        assertTrue(alarmReply.pendingAction is PendingAssistantAction.SetAlarm)
        val alarm = alarmReply.pendingAction as PendingAssistantAction.SetAlarm
        assertEquals(6, alarm.hour24)
        assertEquals(30, alarm.minute)

        val privacyReply = repo.processPrompt("Send message to Rahul silently", "Vishal Boss", SiyaLanguage.ENGLISH)
        assertFalse(privacyReply.isSampleFallback)
        assertTrue(privacyReply.text.contains("never silently"))

        val fallbackReply = repo.processPrompt("Tell me a motivational quote", "Vishal Boss", SiyaLanguage.HINGLISH)
        assertTrue(fallbackReply.isSampleFallback)
        assertTrue(fallbackReply.text.contains("[Sample Response]"))
    }

    @Test
    fun `system action helper parses evening and morning alarms accurately`() {
        val morning = SystemActionHelper.parseAlarmFromText("Subah 6:15 ka alarm lagao")
        assertEquals(6, morning.hour24)
        assertEquals(15, morning.minute)

        val evening = SystemActionHelper.parseAlarmFromText("Set alarm for 7:45 pm")
        assertEquals(19, evening.hour24)
        assertEquals(45, evening.minute)
    }
}
