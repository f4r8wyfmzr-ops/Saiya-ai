package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.system.VoiceRecognitionState
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.components.GlowingMicButton
import com.example.ui.components.SiyaAvatar
import com.example.ui.theme.AmberNotice
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepCosmicPurple
import com.example.ui.theme.DeepRoyalPurple
import com.example.ui.theme.ElectricAmethyst
import com.example.ui.theme.ElevatedVioletSurface
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.ListeningRose
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RadiantOrchid
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletBorder
import com.example.viewmodel.AssistantMood
import com.example.viewmodel.SiyaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class QuickChipItem(
    val label: String,
    val prompt: String,
    val icon: ImageVector
)

@Composable
fun ChatScreen(
    uiState: SiyaUiState,
    onSendMessage: (String) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onClearVoiceError: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    onSpeakGreeting: () -> Unit,
    onStopSpeaking: () -> Unit,
    onCycleLanguage: () -> Unit,
    onConfirmAction: () -> Unit,
    onDismissAction: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefreshTelemetry: () -> Unit,
    onDismissFirebaseNotice: () -> Unit,
    onClearSnackbar: () -> Unit,
    onNotifyPermissionDenied: (String) -> Unit
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    var showMicRationaleDialog by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    val micDeniedMsg = stringResource(R.string.mic_permission_denied_notice)

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onRefreshTelemetry()
        if (granted) {
            onStartVoice()
        } else {
            onNotifyPermissionDenied(micDeniedMsg)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshTelemetry()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(uiState.statusSnackbar) {
        val msg = uiState.statusSnackbar
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            onClearSnackbar()
        }
    }

    val quickChips = remember {
        listOf(
            QuickChipItem("Time & Date", "What is the current time and date?", Icons.Rounded.Schedule),
            QuickChipItem("Battery", "Check my battery level", Icons.Rounded.BatteryChargingFull),
            QuickChipItem("Open Settings", "Open Settings", Icons.AutoMirrored.Rounded.Launch),
            QuickChipItem("Alarm 7 AM", "Set an alarm for 7:00 am", Icons.Rounded.Alarm),
            QuickChipItem("Reminder", "Remind me to drink water at 6:00 pm", Icons.AutoMirrored.Rounded.EventNote)
        )
    }

    val isListening = uiState.voiceState is VoiceRecognitionState.Listening
    val rmsLevel = (uiState.voiceState as? VoiceRecognitionState.Listening)?.rmsLevel ?: 0f

    Scaffold(
        containerColor = ObsidianBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_screen")
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepRoyalPurple.copy(alpha = 0.28f),
                            DeepCosmicPurple,
                            ObsidianBlack
                        )
                    )
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxSize()
                ) {
                    // Top Futuristic Header
                    ChatTopHeader(
                        uiState = uiState,
                        onStopSpeaking = onStopSpeaking,
                        onOpenSettings = onOpenSettings
                    )

                    // Firebase sample AI notice banner (shown when live Gemini is not enabled)
                    AnimatedVisibility(visible = uiState.showFirebaseNotice) {
                        FirebaseNoticeBanner(
                            onDismiss = onDismissFirebaseNotice,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    // Active Microphone Indicator Banner (Never hidden)
                    AnimatedVisibility(
                        visible = isListening,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        val partial = (uiState.voiceState as? VoiceRecognitionState.Listening)?.partialText.orEmpty()
                        ActiveMicHudBanner(
                            partialText = partial,
                            onStop = onStopVoice,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    // Graceful Speech Error Banner
                    AnimatedVisibility(
                        visible = uiState.voiceState is VoiceRecognitionState.Error,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        val errorMsg = (uiState.voiceState as? VoiceRecognitionState.Error)?.message.orEmpty()
                        SpeechErrorBanner(
                            message = errorMsg,
                            onDismiss = onClearVoiceError,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    // Main Conversation Area
                    if (uiState.messages.isEmpty()) {
                        EmptyConversationStage(
                            uiState = uiState,
                            rmsLevel = rmsLevel,
                            quickChips = quickChips,
                            onSpeakGreeting = onSpeakGreeting,
                            onChipClick = onSendMessage,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("chat_message_list")
                        ) {
                            item(key = "compact_hero") {
                                CompactGreetingStrip(
                                    mood = uiState.mood,
                                    rmsLevel = rmsLevel,
                                    greeting = uiState.greetingMessage,
                                    onSpeakGreeting = onSpeakGreeting
                                )
                            }

                            items(
                                items = uiState.messages,
                                key = { it.id }
                            ) { message ->
                                ChatMessageBubble(
                                    message = message,
                                    onSpeak = { onSpeakMessage(message.text) }
                                )
                            }
                        }

                        // Quick Suggestion Chips above dock
                        QuickActionsRow(
                            chips = quickChips,
                            onChipClick = onSendMessage,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Bottom Input & Glowing Mic Dock
                    BottomVoiceAndTextDock(
                        inputText = inputText,
                        onInputChange = { inputText = it },
                        onSend = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        isListening = isListening,
                        rmsLevel = rmsLevel,
                        languageLabel = uiState.preferences.preferredLanguage.shortLabel,
                        onCycleLanguage = onCycleLanguage,
                        onMicClick = {
                            if (isListening) {
                                onStopVoice()
                            } else if (uiState.hasMicPermission) {
                                onStartVoice()
                            } else {
                                showMicRationaleDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    // Confirmation Dialog before sensitive/external Android Intents
    val pendingAction = uiState.pendingAction
    if (pendingAction != null) {
        ActionConfirmationDialog(
            action = pendingAction,
            onConfirm = onConfirmAction,
            onDismiss = onDismissAction
        )
    }

    // Microphone Permission Rationale Dialog (only shown when user taps mic)
    if (showMicRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showMicRationaleDialog = false },
            containerColor = ElevatedVioletSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text(stringResource(R.string.mic_permission_rationale_title)) },
            text = { Text(stringResource(R.string.mic_permission_rationale_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        showMicRationaleDialog = false
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricAmethyst,
                        contentColor = ObsidianBlack
                    ),
                    modifier = Modifier.testTag("grant_mic_permission_button")
                ) {
                    Text("Continue", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showMicRationaleDialog = false },
                    border = BorderStroke(1.dp, VioletBorder)
                ) {
                    Text("Not Now", color = TextPrimary)
                }
            }
        )
    }
}

@Composable
private fun ChatTopHeader(
    uiState: SiyaUiState,
    onStopSpeaking: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val moodLabel = when (uiState.mood) {
        AssistantMood.LISTENING -> "Listening…"
        AssistantMood.THINKING -> "Thinking…"
        AssistantMood.SPEAKING -> "Speaking…"
        AssistantMood.IDLE -> "Ready for ${uiState.preferences.ownerName}"
    }
    val moodDotColor = when (uiState.mood) {
        AssistantMood.LISTENING -> ListeningRose
        AssistantMood.THINKING -> CyberCyan
        AssistantMood.SPEAKING -> RadiantOrchid
        AssistantMood.IDLE -> EmeraldOnline
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SIYA AI",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(moodDotColor)
                )
            }
            Text(
                text = moodLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.isSpeaking) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ListeningRose.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, ListeningRose.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(onClick = onStopSpeaking)
                        .testTag("stop_speaking_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.StopCircle,
                            contentDescription = stringResource(R.string.cd_stop_speaking),
                            tint = ListeningRose,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Mute",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    }
                }
            } else {
                // Live Battery Telemetry Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MidnightCard,
                    border = BorderStroke(1.dp, VioletBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.battery.isCharging) {
                                Icons.Rounded.BatteryChargingFull
                            } else {
                                Icons.Rounded.BatteryStd
                            },
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${uiState.battery.percentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    }
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MidnightCard)
                    .border(1.dp, VioletBorder, CircleShape)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.cd_open_settings),
                    tint = SoftLavender
                )
            }
        }
    }
}

@Composable
private fun FirebaseNoticeBanner(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AmberNotice.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, AmberNotice.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("firebase_notice_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = AmberNotice,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.firebase_notice_banner),
                style = MaterialTheme.typography.labelSmall,
                color = SoftLavender,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss notice",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveMicHudBanner(
    partialText: String,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ListeningRose.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, ListeningRose.copy(alpha = 0.65f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_mic_hud")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.GraphicEq,
                contentDescription = null,
                tint = ListeningRose,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.mic_active_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = ListeningRose,
                    fontWeight = FontWeight.SemiBold
                )
                if (partialText.isNotBlank()) {
                    Text(
                        text = partialText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            TextButton(onClick = onStop) {
                Text("Stop", color = TextPrimary)
            }
        }
    }
}

@Composable
private fun SpeechErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MidnightCard,
        border = BorderStroke(1.dp, ListeningRose.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("OK", color = SoftLavender)
            }
        }
    }
}

@Composable
private fun EmptyConversationStage(
    uiState: SiyaUiState,
    rmsLevel: Float,
    quickChips: List<QuickChipItem>,
    onSpeakGreeting: () -> Unit,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val isCompactHeight = maxHeight < 360.dp
        val avatarSize = if (isCompactHeight) 96.dp else 156.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = if (isCompactHeight) 6.dp else 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                SiyaAvatar(
                    mood = uiState.mood,
                    rmsLevel = rmsLevel,
                    size = avatarSize
                )

                Spacer(modifier = Modifier.height(if (isCompactHeight) 10.dp else 18.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MidnightCard.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, VioletBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = uiState.greetingMessage,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("greeting_text")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = uiState.currentTimeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ElectricAmethyst.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, ElectricAmethyst.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clickable(onClick = onSpeakGreeting)
                                    .testTag("speak_greeting_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = stringResource(R.string.cd_speak_message),
                                        tint = SoftLavender,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Hear Siya",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SoftLavender
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TRY SAYING OR TAPPING",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                QuickActionsRow(
                    chips = quickChips,
                    onChipClick = onChipClick
                )
            }
        }
    }
}

@Composable
private fun CompactGreetingStrip(
    mood: AssistantMood,
    rmsLevel: Float,
    greeting: String,
    onSpeakGreeting: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MidnightCard.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, VioletBorder.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            SiyaAvatar(
                mood = mood,
                rmsLevel = rmsLevel,
                size = 56.dp
            )
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onSpeakGreeting,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = stringResource(R.string.cd_speak_message),
                    tint = SoftLavender
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    onSpeak: () -> Unit
) {
    val timeLabel = remember(message.timestampMillis) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestampMillis))
    }

    Row(
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isFromUser) 20.dp else 6.dp,
                bottomEnd = if (message.isFromUser) 6.dp else 20.dp
            ),
            color = Color.Transparent,
            border = BorderStroke(
                width = 1.dp,
                color = if (message.isFromUser) {
                    RadiantOrchid.copy(alpha = 0.55f)
                } else {
                    VioletBorder
                }
            ),
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(
                    brush = if (message.isFromUser) {
                        Brush.linearGradient(
                            colors = listOf(DeepRoyalPurple, ElectricAmethyst.copy(alpha = 0.75f))
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(MidnightCard, ElevatedVioletSurface)
                        )
                    },
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (message.isFromUser) 20.dp else 6.dp,
                        bottomEnd = if (message.isFromUser) 6.dp else 20.dp
                    )
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!message.isFromUser && (message.isSampleAiResponse || !message.actionLabel.isNullOrBlank())) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        if (message.isSampleAiResponse) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = AmberNotice.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = stringResource(R.string.sample_response_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberNotice,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (!message.actionLabel.isNullOrBlank() && message.actionLabel != "Sample AI") {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = CyberCyan.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = message.actionLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isFromUser) SoftLavender else TextMuted
                    )

                    if (!message.isFromUser) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = stringResource(R.string.cd_speak_message),
                                tint = SoftLavender,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    chips: List<QuickChipItem>,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(chips, key = { it.label }) { chip ->
            Surface(
                shape = RoundedCornerShape(50),
                color = MidnightCard,
                border = BorderStroke(1.dp, VioletBorder),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable { onChipClick(chip.prompt) }
                    .testTag("quick_chip_${chip.label.lowercase().replace(" ", "_")}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = chip.icon,
                        contentDescription = null,
                        tint = RadiantOrchid,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomVoiceAndTextDock(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isListening: Boolean,
    rmsLevel: Float,
    languageLabel: String,
    onCycleLanguage: () -> Unit,
    onMicClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        color = DeepCosmicPurple.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, VioletBorder.copy(alpha = 0.8f)),
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.mic_tap_hint),
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MidnightCard,
                        unfocusedContainerColor = MidnightCard,
                        focusedBorderColor = ElectricAmethyst,
                        unfocusedBorderColor = VioletBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = RadiantOrchid
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input")
                )

                IconButton(
                    onClick = onSend,
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) ElectricAmethyst else MidnightCard
                        )
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = stringResource(R.string.cd_send_message),
                        tint = if (inputText.isNotBlank()) ObsidianBlack else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MidnightCard,
                    border = BorderStroke(1.dp, VioletBorder),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(onClick = onCycleLanguage)
                        .testTag("language_cycle_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = "Switch language",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = languageLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                    }
                }

                GlowingMicButton(
                    isListening = isListening,
                    rmsLevel = rmsLevel,
                    onClick = onMicClick
                )

                Text(
                    text = if (isListening) "Listening" else "Voice Ready",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isListening) ListeningRose else TextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(84.dp)
                )
            }
        }
    }
}
