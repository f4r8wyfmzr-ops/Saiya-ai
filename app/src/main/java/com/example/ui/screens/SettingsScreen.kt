package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.SiyaLanguage
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
import com.example.viewmodel.SiyaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SiyaUiState,
    onBack: () -> Unit,
    onSaveOwnerName: (String) -> Unit,
    onSelectLanguage: (SiyaLanguage) -> Unit,
    onToggleAutoSpeak: (Boolean) -> Unit,
    onChangeSpeechRate: (Float) -> Unit,
    onChangeSpeechPitch: (Float) -> Unit,
    onSelectVoice: (String) -> Unit,
    onTestVoice: () -> Unit,
    onToggleSaveHistory: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onResetPreferences: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onRefreshTelemetry: () -> Unit
) {
    var ownerDraft by rememberSaveable(uiState.preferences.ownerName) {
        mutableStateOf(uiState.preferences.ownerName)
    }
    var showVoiceDropdown by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshTelemetry()
    }

    Scaffold(
        containerColor = ObsidianBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                            tint = SoftLavender
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepRoyalPurple.copy(alpha = 0.25f),
                            DeepCosmicPurple,
                            ObsidianBlack
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // 1. Owner & Language Preferences
                SettingsSectionCard(
                    title = "Owner & Language",
                    icon = Icons.Rounded.Person
                ) {
                    Text(
                        text = "Preferred Name",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = ownerDraft,
                            onValueChange = { ownerDraft = it },
                            singleLine = true,
                            placeholder = { Text("Vishal Boss", color = TextMuted) },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DeepCosmicPurple,
                                unfocusedContainerColor = DeepCosmicPurple,
                                focusedBorderColor = ElectricAmethyst,
                                unfocusedBorderColor = VioletBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("owner_name_input")
                        )
                        Button(
                            onClick = { onSaveOwnerName(ownerDraft) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricAmethyst,
                                contentColor = ObsidianBlack
                            ),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("save_owner_name_button")
                        ) {
                            Text("Save", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Conversation Language",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SiyaLanguage.entries.forEach { lang ->
                            val selected = uiState.preferences.preferredLanguage == lang
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) ElectricAmethyst.copy(alpha = 0.2f) else DeepCosmicPurple,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) ElectricAmethyst else VioletBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .clickable { onSelectLanguage(lang) }
                                    .testTag("lang_option_${lang.name.lowercase()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary
                                    )
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = ElectricAmethyst,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Voice & Text-to-Speech
                SettingsSectionCard(
                    title = "Siya Voice & Speech",
                    icon = Icons.Rounded.RecordVoiceOver
                ) {
                    SettingsSwitchRow(
                        title = "Auto-Speak Responses",
                        subtitle = "Siya reads replies aloud automatically",
                        checked = uiState.preferences.autoSpeakEnabled,
                        onCheckedChange = onToggleAutoSpeak,
                        testTag = "auto_speak_switch"
                    )

                    HorizontalDivider(
                        color = VioletBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Text(
                        text = "Speech Rate: ${"%.2f".format(uiState.preferences.speechRate)}x",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Slider(
                        value = uiState.preferences.speechRate,
                        onValueChange = onChangeSpeechRate,
                        valueRange = 0.6f..1.6f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricAmethyst,
                            activeTrackColor = ElectricAmethyst,
                            inactiveTrackColor = VioletBorder
                        )
                    )

                    Text(
                        text = "Voice Pitch: ${"%.2f".format(uiState.preferences.speechPitch)}x",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Slider(
                        value = uiState.preferences.speechPitch,
                        onValueChange = onChangeSpeechPitch,
                        valueRange = 0.7f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = RadiantOrchid,
                            activeTrackColor = RadiantOrchid,
                            inactiveTrackColor = VioletBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val selectedVoiceLabel = uiState.availableVoices
                        .firstOrNull { it.name == uiState.preferences.selectedVoiceName }
                        ?.displayLabel
                        ?: "Auto (Best Natural Female Voice)"

                    Box {
                        OutlinedButton(
                            onClick = { showVoiceDropdown = true },
                            border = BorderStroke(1.dp, VioletBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Voice: $selectedVoiceLabel",
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        DropdownMenu(
                            expanded = showVoiceDropdown,
                            onDismissRequest = { showVoiceDropdown = false },
                            containerColor = ElevatedVioletSurface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Auto (Best Natural Female Voice)", color = TextPrimary) },
                                onClick = {
                                    onSelectVoice("")
                                    showVoiceDropdown = false
                                }
                            )
                            uiState.availableVoices.take(12).forEach { voice ->
                                DropdownMenuItem(
                                    text = { Text(voice.displayLabel, color = TextPrimary) },
                                    onClick = {
                                        onSelectVoice(voice.name)
                                        showVoiceDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onTestVoice,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricAmethyst.copy(alpha = 0.22f),
                            contentColor = SoftLavender
                        ),
                        border = BorderStroke(1.dp, ElectricAmethyst.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Test Siya's Voice")
                    }
                }

                // 3. Permissions & Privacy
                SettingsSectionCard(
                    title = "Permissions & Privacy",
                    icon = Icons.Rounded.Security
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = null,
                                tint = if (uiState.hasMicPermission) EmeraldOnline else AmberNotice
                            )
                            Column {
                                Text(
                                    text = "Microphone Access",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (uiState.hasMicPermission) {
                                        "Granted (Only active on tap)"
                                    } else {
                                        "Not granted (Requested only when you tap mic)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (uiState.hasMicPermission) EmeraldOnline else TextSecondary
                                )
                            }
                        }

                        if (!uiState.hasMicPermission) {
                            TextButton(
                                onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                            ) {
                                Text("Grant", color = CyberCyan)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onOpenAppSettings,
                        border = BorderStroke(1.dp, VioletBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manage_system_permissions_button")
                    ) {
                        Text("Manage System Permissions", color = TextPrimary)
                    }

                    HorizontalDivider(
                        color = VioletBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    SettingsSwitchRow(
                        title = "Remember Chat History Locally",
                        subtitle = "Store conversations privately on this phone only",
                        checked = uiState.preferences.saveChatHistory,
                        onCheckedChange = onToggleSaveHistory,
                        testTag = "save_history_switch"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PrivacyPoint("No hidden background recording — active mic indicator always shown.")
                    PrivacyPoint("Explicit confirmation dialog before opening apps, alarms, or reminders.")
                    PrivacyPoint("Never silently sends messages, makes calls, or deletes files.")
                    PrivacyPoint("No contacts, SMS, or personal data collected without permission.")

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            border = BorderStroke(1.dp, ListeningRose.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = null,
                                tint = ListeningRose,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Clear Chat", color = ListeningRose)
                        }

                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            border = BorderStroke(1.dp, VioletBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reset_prefs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RestartAlt,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Reset", color = TextSecondary)
                        }
                    }
                }

                // 4. Local Reminders (if any)
                if (uiState.reminders.isNotEmpty()) {
                    SettingsSectionCard(
                        title = "Saved Reminders",
                        icon = Icons.Rounded.WarningAmber
                    ) {
                        uiState.reminders.forEach { reminder ->
                            val dateStr = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
                                .format(Date(reminder.triggerTimeMillis))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reminder.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan
                                    )
                                }
                                IconButton(onClick = { onDeleteReminder(reminder.id) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete reminder",
                                        tint = ListeningRose
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. AI Engine Status Note
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MidnightCard,
                    border = BorderStroke(1.dp, VioletBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SIYA AI • v1.0",
                            style = MaterialTheme.typography.labelLarge,
                            color = SoftLavender
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Currently running in Sample AI Mode for open-ended chat (Firebase AI Logic was declined). All voice recognition, TTS, battery/date/time, app launching, alarms, timers, and local memory features are live.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = ElevatedVioletSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Clear Chat History?") },
            text = { Text("This will permanently delete all locally saved conversation messages.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearHistory()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ListeningRose,
                        contentColor = ObsidianBlack
                    )
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = TextPrimary)
                }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = ElevatedVioletSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Reset Preferences?") },
            text = { Text("Restore owner name to Vishal Boss, Hinglish language, and default voice settings?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetPreferences()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricAmethyst,
                        contentColor = ObsidianBlack
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = TextPrimary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MidnightCard.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, VioletBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricAmethyst,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBlack,
                checkedTrackColor = ElectricAmethyst,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = DeepCosmicPurple
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun PrivacyPoint(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = CyberCyan,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
