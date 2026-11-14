package com.example.data.ai

import com.example.data.local.SiyaLanguage

sealed interface PendingAssistantAction {
    val summaryTitle: String
    val summaryDescription: String

    data class OpenApp(
        val appName: String,
        val packageName: String
    ) : PendingAssistantAction {
        override val summaryTitle: String = "Open $appName?"
        override val summaryDescription: String =
            "Siya will launch $appName ($packageName) on your phone."
    }

    data class SetAlarm(
        val hour24: Int,
        val minute: Int,
        val label: String
    ) : PendingAssistantAction {
        override val summaryTitle: String = "Set Alarm for %02d:%02d?".format(hour24, minute)
        override val summaryDescription: String =
            "Siya will open your Clock app to save the alarm \"$label\" at %02d:%02d.".format(hour24, minute)
    }

    data class SetTimer(
        val durationSeconds: Int,
        val label: String
    ) : PendingAssistantAction {
        private val minutes = durationSeconds / 60
        private val seconds = durationSeconds % 60
        override val summaryTitle: String =
            if (minutes > 0) "Start ${minutes}m ${if (seconds > 0) "${seconds}s " else ""}Timer?"
            else "Start ${seconds}s Timer?"
        override val summaryDescription: String =
            "Siya will open your Clock app to confirm this timer."
    }

    data class CreateReminder(
        val title: String,
        val triggerTimeMillis: Long,
        val formattedTime: String
    ) : PendingAssistantAction {
        override val summaryTitle: String = "Save Reminder?"
        override val summaryDescription: String =
            "\"$title\" around $formattedTime — Siya will save this locally and open Calendar so you can confirm."
    }
}

data class AiReply(
    val text: String,
    val isSampleFallback: Boolean = false,
    val pendingAction: PendingAssistantAction? = null,
    val actionLabel: String? = null
)

interface AiRepository {
    val isLiveGeminiEnabled: Boolean

    suspend fun processPrompt(
        prompt: String,
        ownerName: String,
        language: SiyaLanguage
    ): AiReply
}
