package com.example.data.ai

import com.example.data.local.SiyaLanguage
import com.example.system.AppLauncherHelper
import com.example.system.DeviceInfoHelper
import com.example.system.SystemActionHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local + Sample AI implementation used when Firebase AI Logic is not enabled.
 * Handles all device telemetry, app launching, alarms, timers, and reminders locally,
 * and returns clearly labeled sample responses for open-ended Gemini queries.
 */
class SampleAiRepository(
    private val deviceInfoHelper: DeviceInfoHelper,
    private val appLauncherHelper: AppLauncherHelper
) : AiRepository {

    override val isLiveGeminiEnabled: Boolean = false

    override suspend fun processPrompt(
        prompt: String,
        ownerName: String,
        language: SiyaLanguage
    ): AiReply {
        val trimmed = prompt.trim()
        val lower = trimmed.lowercase()

        // 1. Privacy & Safety guardrails — never silently send messages, call, delete files, or alter settings
        if (containsRestrictedSilentAction(lower)) {
            val reply = when (language) {
                SiyaLanguage.HINDI ->
                    "$ownerName, aapki privacy aur safety mere liye sabse zaroori hai. Main bina aapki permission ke silently koi call, message, file delete ya system setting change nahi karti."
                SiyaLanguage.ENGLISH ->
                    "For your safety and privacy, $ownerName, I never silently send messages, make calls, delete files, or modify system settings."
                SiyaLanguage.HINGLISH ->
                    "$ownerName, aapki safety aur privacy sabse important hai! Main bina aapki explicit permission ke silently calls, messages, file deletions ya system settings change nahi karti."
            }
            return AiReply(text = reply, isSampleFallback = false, actionLabel = "Privacy Guard")
        }

        // 2. Battery level check (Real Android BatteryManager)
        if (lower.contains("battery") || lower.contains("charge") || lower.contains("charging") || lower.contains("बैटरी")) {
            val battery = deviceInfoHelper.getBatteryTelemetry()
            val reply = when (language) {
                SiyaLanguage.HINDI ->
                    "$ownerName, abhi aapke phone ki battery ${battery.percentage}% hai (${battery.statusLabel})."
                SiyaLanguage.ENGLISH ->
                    "$ownerName, your battery is currently at ${battery.percentage}% (${battery.statusLabel})."
                SiyaLanguage.HINGLISH ->
                    "$ownerName, abhi phone ki battery ${battery.percentage}% hai (${battery.statusLabel}). ${if (battery.percentage <= 20 && !battery.isCharging) "Charger laga lena achha rahega!" else "Sab badhiya chal raha hai!"}"
            }
            return AiReply(text = reply, isSampleFallback = false, actionLabel = "Battery ${battery.percentage}%")
        }

        // 3. Date & Time check (Real system clock)
        if (isDateOrTimeQuery(lower)) {
            val time = deviceInfoHelper.getFormattedTime()
            val date = deviceInfoHelper.getFormattedDate(language)
            val reply = when (language) {
                SiyaLanguage.HINDI ->
                    "$ownerName, abhi samay $time hua hai aur aaj $date hai."
                SiyaLanguage.ENGLISH ->
                    "$ownerName, it is currently $time on $date."
                SiyaLanguage.HINGLISH ->
                    "$ownerName, abhi time $time ho raha hai aur aaj ki date $date hai."
            }
            return AiReply(text = reply, isSampleFallback = false, actionLabel = time)
        }

        // 4. Alarm intent (Requires user confirmation)
        if (lower.contains("alarm") || lower.contains("अलार्म")) {
            val parsed = SystemActionHelper.parseAlarmFromText(trimmed)
            val timeStr = "%02d:%02d".format(parsed.hour24, parsed.minute)
            val reply = when (language) {
                SiyaLanguage.HINDI ->
                    "Bilkul $ownerName! Main $timeStr baje ka alarm set karne ke liye taiyaar hoon. Kripya neeche confirm karein."
                SiyaLanguage.ENGLISH ->
                    "Sure $ownerName! I've prepared an alarm for $timeStr. Please confirm below so I can open your Clock app."
                SiyaLanguage.HINGLISH ->
                    "Ji $ownerName! Maine $timeStr ka alarm ready kar diya hai. Neeche Confirm button dabayein to main Clock app khol doon."
            }
            return AiReply(
                text = reply,
                isSampleFallback = false,
                pendingAction = PendingAssistantAction.SetAlarm(parsed.hour24, parsed.minute, parsed.label),
                actionLabel = "Alarm $timeStr"
            )
        }

        // 5. Timer intent (Requires user confirmation)
        if (lower.contains("timer") || lower.contains("टाइमर")) {
            val seconds = SystemActionHelper.parseTimerSecondsFromText(trimmed)
            val mins = seconds / 60
            val label = if (mins > 0) "$mins min" else "$seconds sec"
            val reply = when (language) {
                SiyaLanguage.HINDI ->
                    "$ownerName, $label ka timer ready hai. Kripya confirm karein."
                SiyaLanguage.ENGLISH ->
                    "Got it $ownerName! Ready to start a $label timer. Please confirm below."
                SiyaLanguage.HINGLISH ->
                    "Done $ownerName! $label ka timer ready hai — confirm karte hi Clock app mein start ho jayega."
            }
            return AiReply(
                text = reply,
                isSampleFallback = false,
                pendingAction = PendingAssistantAction.SetTimer(seconds, "SIYA AI Timer"),
                actionLabel = "Timer $label"
            )
        }

        // 6. Reminder intent (Requires user confirmation)
        if (lower.contains("remind") || lower.contains("reminder") || lower.contains("yaad") || lower.contains("रिमाइंडर")) {
            val title = extractReminderTitle(trimmed)
            val triggerMillis = SystemActionHelper.parseReminderTriggerMillis(trimmed)
            val formattedTime = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(triggerMillis))
            val reply = when (language) {
                SiyaLanguage.HINDI ->
                    "$ownerName, \"$title\" ke liye $formattedTime par reminder ready hai. Kripya neeche confirm karein."
                SiyaLanguage.ENGLISH ->
                    "Sure $ownerName! I prepared a reminder for \"$title\" at $formattedTime. Please confirm below."
                SiyaLanguage.HINGLISH ->
                    "Bilkul $ownerName! \"$title\" ka reminder $formattedTime ke liye ready hai. Confirm karein to main save aur Calendar open kar doon."
            }
            return AiReply(
                text = reply,
                isSampleFallback = false,
                pendingAction = PendingAssistantAction.CreateReminder(title, triggerMillis, formattedTime),
                actionLabel = "Reminder"
            )
        }

        // 7. Open installed app intent (Requires user confirmation)
        val requestedAppQuery = extractOpenAppTarget(trimmed)
        if (requestedAppQuery != null) {
            val match = appLauncherHelper.findMatchingApp(requestedAppQuery)
            return if (match != null) {
                val reply = when (language) {
                    SiyaLanguage.HINDI ->
                        "$ownerName, mujhe ${match.appName} mil gaya hai. Kholne ke liye neeche confirm karein."
                    SiyaLanguage.ENGLISH ->
                        "$ownerName, I found ${match.appName}. Please confirm below to open it."
                    SiyaLanguage.HINGLISH ->
                        "$ownerName, ${match.appName} open karne ke liye ready hai! Confirm karte hi launch kar doongi."
                }
                AiReply(
                    text = reply,
                    isSampleFallback = false,
                    pendingAction = PendingAssistantAction.OpenApp(match.appName, match.packageName),
                    actionLabel = "Open ${match.appName}"
                )
            } else {
                val someApps = appLauncherHelper.getLaunchableApps().take(5).joinToString(", ") { it.appName }
                val reply = when (language) {
                    SiyaLanguage.HINDI ->
                        "Maaf kijiye $ownerName, mujhe \"$requestedAppQuery\" app nahi mila.${if (someApps.isNotBlank()) " Aap inmein se try kar sakte hain: $someApps." else ""}"
                    SiyaLanguage.ENGLISH ->
                        "Sorry $ownerName, I couldn't find an installed app matching \"$requestedAppQuery\".${if (someApps.isNotBlank()) " Available apps include: $someApps." else ""}"
                    SiyaLanguage.HINGLISH ->
                        "Sorry $ownerName, \"$requestedAppQuery\" app device par nahi mila.${if (someApps.isNotBlank()) " Yeh apps available hain: $someApps." else ""}"
                }
                AiReply(text = reply, isSampleFallback = false)
            }
        }

        // 8. Greetings, persona & capabilities
        if (isGreetingOrIdentity(lower)) {
            val reply = when (language) {
                SiyaLanguage.HINDI ->
                    "Namaste $ownerName! Main Siya hoon, aapki friendly AI voice assistant. Main aapko samay, tarikh, battery bata sakti hoon, apps khol sakti hoon aur alarms ya reminders set kar sakti hoon. Bataiye aaj kya madad karoon?"
                SiyaLanguage.ENGLISH ->
                    "Hello $ownerName! I'm Siya, your friendly voice assistant. I can check the date, time, and battery, open installed apps, and prepare alarms or reminders with your confirmation. How can I help today?"
                SiyaLanguage.HINGLISH ->
                    "Hello $ownerName! Siya is here and always happy to help. Aap mujhse time, date, battery pooch sakte hain, koi app open karwa sakte hain, ya alarm aur reminder set kar sakte hain. Bataiye aaj kya karna hai?"
            }
            return AiReply(text = reply, isSampleFallback = false)
        }

        // 9. Open-ended conversational Gemini fallback (Clearly labeled sample response)
        val sampleResponse = when (language) {
            SiyaLanguage.HINDI ->
                "[Sample Response] Namaste $ownerName! Aapne poochha: \"$trimmed\". Yeh ek local sample AI jawaab hai kyunki live Gemini cloud connection (Firebase AI Logic) abhi enabled nahi hai. Jab Firebase enable hoga, tab main aapke har sawal ka poora Gemini AI jawaab doongi!"
            SiyaLanguage.ENGLISH ->
                "[Sample Response] Hi $ownerName! I heard \"$trimmed\". This is a clearly labeled sample response because live Gemini cloud AI requires Firebase AI Logic to be enabled. Meanwhile, all voice, date/time, battery, app launch, alarm, and reminder features are fully active!"
            SiyaLanguage.HINGLISH ->
                "[Sample Response] Ji $ownerName! Aapka message \"$trimmed\" mila. Abhi main sample AI mode mein hoon kyunki live Gemini AI ke liye Firebase AI Logic enable karna zaroori hai. Tab tak mere saare local voice commands (time, battery, open app, alarm, reminder) 100% live kaam kar rahe hain!"
        }
        return AiReply(
            text = sampleResponse,
            isSampleFallback = true,
            actionLabel = "Sample AI"
        )
    }

    private fun containsRestrictedSilentAction(lower: String): Boolean {
        return (lower.contains("send") && (lower.contains("sms") || lower.contains("message") || lower.contains("whatsapp"))) ||
            (lower.contains("bhej") && (lower.contains("message") || lower.contains("sms"))) ||
            (lower.contains("delete") && (lower.contains("file") || lower.contains("photo") || lower.contains("contact"))) ||
            (lower.contains("call ") && !lower.contains("called")) ||
            lower.contains("phone lagao") ||
            lower.contains("wifi off") ||
            lower.contains("bluetooth off")
    }

    private fun isDateOrTimeQuery(lower: String): Boolean {
        val keywords = listOf(
            "time", "date", "today", "day is it", "samay", "baje", "waqt",
            "tarikh", "tareekh", "aaj kya", "din", "समय", "तारीख", "बजे"
        )
        return keywords.any { lower.contains(it) }
    }

    private fun isGreetingOrIdentity(lower: String): Boolean {
        val keywords = listOf(
            "hello", "hi ", "hi!", "hey", "namaste", "good morning", "good evening",
            "good afternoon", "who are you", "what can you do", "kya haal", "kaise ho",
            "kaun ho", "kya kar sakti", "siya"
        )
        return lower == "hi" || lower == "hello" || keywords.any { lower.contains(it) }
    }

    private fun extractOpenAppTarget(raw: String): String? {
        val lower = raw.lowercase().trim()
        val patterns = listOf(
            Regex("""^(?:please\s+)?(?:open|launch|start)\s+(.+)$"""),
            Regex("""^(.+?)\s+(?:kholo|khol do|open karo|chalu karo)$""")
        )
        for (regex in patterns) {
            val match = regex.find(lower)
            if (match != null) {
                return match.groupValues[1]
                    .replace("app", "")
                    .replace("the", "")
                    .trim()
                    .takeIf { it.isNotBlank() }
            }
        }
        return null
    }

    private fun extractReminderTitle(raw: String): String {
        val cleaned = raw
            .replace(Regex("""(?i)^(please\s+)?(remind me to|set a reminder to|set reminder to|create reminder|reminder lagao|yaad dilana)\s*"""), "")
            .replace(Regex("""(?i)\s+at\s+\d{1,2}(:\d{2})?\s*(am|pm)?$"""), "")
            .trim()
        return cleaned.ifBlank { "Important Task" }
    }
}
