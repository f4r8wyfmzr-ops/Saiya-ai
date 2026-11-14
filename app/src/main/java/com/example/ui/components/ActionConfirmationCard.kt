package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.ai.PendingAssistantAction
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricAmethyst
import com.example.ui.theme.ElevatedVioletSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletBorder

@Composable
fun ActionConfirmationDialog(
    action: PendingAssistantAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val icon = when (action) {
        is PendingAssistantAction.OpenApp -> Icons.AutoMirrored.Rounded.Launch
        is PendingAssistantAction.SetAlarm -> Icons.Rounded.Alarm
        is PendingAssistantAction.SetTimer -> Icons.Rounded.Timer
        is PendingAssistantAction.CreateReminder -> Icons.Rounded.EventAvailable
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ElevatedVioletSurface,
            border = BorderStroke(1.dp, VioletBorder),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("action_confirmation_dialog")
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ElectricAmethyst.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, ElectricAmethyst.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = action.summaryTitle,
                            tint = SoftLavender,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Confirm Action",
                            style = MaterialTheme.typography.labelLarge,
                            color = CyberCyan
                        )
                        Text(
                            text = action.summaryTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = action.summaryDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Siya never performs phone actions silently.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, VioletBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_action_button")
                    ) {
                        Text("Cancel", color = TextPrimary)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricAmethyst,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_action_button")
                    ) {
                        Text("Confirm", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
