package com.example.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import java.util.Calendar

data class ParsedAlarm(
    val hour24: Int,
    val minute: Int,
    val label: String
)

class SystemActionHelper(private val context: Context) {

    fun openAlarmIntent(hour24: Int, minute: Int, label: String): Boolean {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour24.coerceIn(0, 23))
            putExtra(AlarmClock.EXTRA_MINUTES, minute.coerceIn(0, 59))
            putExtra(AlarmClock.EXTRA_MESSAGE, label.ifBlank { "SIYA AI Alarm" })
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openTimerIntent(durationSeconds: Int, label: String): Boolean {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, durationSeconds.coerceAtLeast(10))
            putExtra(AlarmClock.EXTRA_MESSAGE, label.ifBlank { "SIYA AI Timer" })
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openReminderCalendarIntent(title: String, triggerTimeMillis: Long): Boolean {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title.ifBlank { "SIYA AI Reminder" })
            putExtra(CalendarContract.Events.DESCRIPTION, "Created via SIYA AI Assistant")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, triggerTimeMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, triggerTimeMillis + 30 * 60 * 1000L)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openAppDetailsSettings(): Boolean {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    companion object {
        fun parseAlarmFromText(input: String): ParsedAlarm {
            val lower = input.lowercase()
            val timeRegex = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm|a\.m\.|p\.m\.)?""")
            val match = timeRegex.find(lower)
            var hour = match?.groupValues?.get(1)?.toIntOrNull() ?: 7
            val minute = match?.groupValues?.get(2)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
            val amPm = match?.groupValues?.get(3).orEmpty()

            val isPmHint = amPm.startsWith("p") ||
                lower.contains("shaam") ||
                lower.contains("sham") ||
                lower.contains("raat") ||
                lower.contains("evening") ||
                lower.contains("night")
            val isAmHint = amPm.startsWith("a") ||
                lower.contains("subah") ||
                lower.contains("morning")

            if (isPmHint && hour in 1..11) {
                hour += 12
            } else if (isAmHint && hour == 12) {
                hour = 0
            }
            hour = hour.coerceIn(0, 23)

            return ParsedAlarm(
                hour24 = hour,
                minute = minute,
                label = "SIYA AI Alarm"
            )
        }

        fun parseTimerSecondsFromText(input: String): Int {
            val lower = input.lowercase()
            val match = Regex("""(\d{1,3})\s*(hour|hr|hrs|ghanta|minute|min|mins|second|sec|secs)?""").find(lower)
            val value = match?.groupValues?.get(1)?.toIntOrNull() ?: 5
            val unit = match?.groupValues?.get(2).orEmpty()
            return when {
                unit.startsWith("hour") || unit.startsWith("hr") || unit.startsWith("ghanta") -> value * 3600
                unit.startsWith("sec") -> value
                else -> value * 60
            }.coerceIn(10, 86400)
        }

        fun parseReminderTriggerMillis(input: String, nowMillis: Long = System.currentTimeMillis()): Long {
            val lower = input.lowercase()
            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val timeRegex = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""")
            val match = timeRegex.find(lower)
            if (match != null) {
                var hour = match.groupValues[1].toIntOrNull() ?: return nowMillis + 3600_000L
                val minute = match.groupValues[2].toIntOrNull()?.coerceIn(0, 59) ?: 0
                val amPm = match.groupValues[3]
                if (amPm == "pm" && hour in 1..11) hour += 12
                if (amPm == "am" && hour == 12) hour = 0
                cal.set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                if (cal.timeInMillis <= nowMillis) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                return cal.timeInMillis
            }
            return nowMillis + 60 * 60 * 1000L
        }
    }
}
