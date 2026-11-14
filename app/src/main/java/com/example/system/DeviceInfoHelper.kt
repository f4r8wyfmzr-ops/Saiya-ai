package com.example.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.data.local.SiyaLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BatteryTelemetry(
    val percentage: Int,
    val isCharging: Boolean,
    val statusLabel: String
)

class DeviceInfoHelper(private val context: Context) {

    fun getBatteryTelemetry(): BatteryTelemetry {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) {
            ((level * 100f) / scale).toInt().coerceIn(0, 100)
        } else {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(0, 100) ?: 100
        }

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val statusLabel = when (status) {
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "On Battery"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Ready"
        }

        return BatteryTelemetry(
            percentage = pct,
            isCharging = isCharging,
            statusLabel = statusLabel
        )
    }

    fun getFormattedTime(nowMillis: Long = System.currentTimeMillis()): String {
        val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return fmt.format(Date(nowMillis))
    }

    fun getFormattedDate(language: SiyaLanguage, nowMillis: Long = System.currentTimeMillis()): String {
        val locale = when (language) {
            SiyaLanguage.HINDI -> Locale.forLanguageTag("hi-IN")
            SiyaLanguage.HINGLISH -> Locale.forLanguageTag("en-IN")
            SiyaLanguage.ENGLISH -> Locale.getDefault()
        }
        val fmt = SimpleDateFormat("EEEE, dd MMMM yyyy", locale)
        return fmt.format(Date(nowMillis))
    }
}
