package personal.smsforwarder.service

import android.content.Context
import android.content.Intent

/**
 * Helper to store and retrieve notification stats for the foreground service.
 */
object NotificationStats {
    private const val PREFS_NAME = "notification_stats"
    private const val KEY_TOTAL_FORWARDED = "total_forwarded"
    private const val KEY_TOTAL_FAILED = "total_failed"
    private const val KEY_LAST_SENDER = "last_sender"
    private const val KEY_LAST_RULE = "last_rule"
    private const val KEY_LAST_TIME = "last_time"
    private const val KEY_TODAY_COUNT = "today_count"
    private const val KEY_TODAY_DATE = "today_date"

    const val ACTION_STATS_UPDATED = "personal.smsforwarder.STATS_UPDATED"

    fun recordForward(context: Context, sender: String, ruleName: String, success: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        val savedDate = prefs.getString(KEY_TODAY_DATE, "")

        prefs.edit().apply {
            if (success) {
                putLong(KEY_TOTAL_FORWARDED, prefs.getLong(KEY_TOTAL_FORWARDED, 0) + 1)
                putString(KEY_LAST_SENDER, sender)
                putString(KEY_LAST_RULE, ruleName)
                putLong(KEY_LAST_TIME, System.currentTimeMillis())

                // Reset daily count if new day
                if (savedDate != today) {
                    putString(KEY_TODAY_DATE, today)
                    putInt(KEY_TODAY_COUNT, 1)
                } else {
                    putInt(KEY_TODAY_COUNT, prefs.getInt(KEY_TODAY_COUNT, 0) + 1)
                }
            } else {
                putLong(KEY_TOTAL_FAILED, prefs.getLong(KEY_TOTAL_FAILED, 0) + 1)
            }
            apply()
        }

        // Notify service to update notification
        context.sendBroadcast(Intent(ACTION_STATS_UPDATED).setPackage(context.packageName))
    }

    fun getStats(context: Context): Stats {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        val savedDate = prefs.getString(KEY_TODAY_DATE, "")

        val todayCount = if (savedDate == today) {
            prefs.getInt(KEY_TODAY_COUNT, 0)
        } else {
            0
        }

        return Stats(
            totalForwarded = prefs.getLong(KEY_TOTAL_FORWARDED, 0),
            totalFailed = prefs.getLong(KEY_TOTAL_FAILED, 0),
            todayCount = todayCount,
            lastSender = prefs.getString(KEY_LAST_SENDER, null),
            lastRule = prefs.getString(KEY_LAST_RULE, null),
            lastTime = prefs.getLong(KEY_LAST_TIME, 0)
        )
    }

    fun formatLastTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            else -> "${diff / 86400_000}d ago"
        }
    }

    data class Stats(
        val totalForwarded: Long,
        val totalFailed: Long,
        val todayCount: Int,
        val lastSender: String?,
        val lastRule: String?,
        val lastTime: Long
    )
}
