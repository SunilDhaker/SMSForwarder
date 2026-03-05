package personal.smsforwarder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import personal.smsforwarder.MainActivity
import personal.smsforwarder.R

class ForwarderForegroundService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val notificationRefreshRunnable = object : Runnable {
        override fun run() {
            // Re-post notification to ensure it stays visible (Android 14+ can dismiss it)
            updateNotification()
            handler.postDelayed(this, NOTIFICATION_REFRESH_INTERVAL)
        }
    }

    private val statsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NotificationStats.ACTION_STATS_UPDATED) {
                updateNotification()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Register receiver for stats updates
        val filter = IntentFilter(NotificationStats.ACTION_STATS_UPDATED)
        ContextCompat.registerReceiver(
            this,
            statsUpdateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                handler.removeCallbacks(notificationRefreshRunnable)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // On Android 14+, periodically refresh notification in case user dismisses it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            handler.removeCallbacks(notificationRefreshRunnable)
            handler.postDelayed(notificationRefreshRunnable, NOTIFICATION_REFRESH_INTERVAL)
        }

        // START_STICKY ensures the service restarts if killed by the system
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(notificationRefreshRunnable)
        try {
            unregisterReceiver(statsUpdateReceiver)
        } catch (_: Exception) { }
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "SMS Forwarder Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps SMS Forwarder running in the background"
            setShowBadge(false)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        // Intent to open the app when notification is tapped
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to stop the service
        val stopIntent = Intent(this, ForwarderForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get stats for notification content
        val stats = NotificationStats.getStats(this)
        val contentText = buildNotificationText(stats)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SMS Forwarder Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setStyle(NotificationCompat.BigTextStyle().bigText(buildExpandedText(stats)))
            .build()

        // Make notification truly non-dismissable
        notification.flags = notification.flags or
                Notification.FLAG_NO_CLEAR or
                Notification.FLAG_ONGOING_EVENT

        return notification
    }

    private fun buildNotificationText(stats: NotificationStats.Stats): String {
        return if (stats.totalForwarded == 0L) {
            "Monitoring for incoming SMS"
        } else {
            "Today: ${stats.todayCount} | Total: ${stats.totalForwarded}"
        }
    }

    private fun buildExpandedText(stats: NotificationStats.Stats): String {
        if (stats.totalForwarded == 0L) {
            return "Monitoring for incoming SMS messages.\nNo messages forwarded yet."
        }

        val lastInfo = if (stats.lastSender != null && stats.lastTime > 0) {
            val timeAgo = NotificationStats.formatLastTime(stats.lastTime)
            "\nLast: ${stats.lastSender} ($timeAgo)"
        } else ""

        val ruleInfo = if (stats.lastRule != null) {
            "\nRule: ${stats.lastRule}"
        } else ""

        val failInfo = if (stats.totalFailed > 0) {
            "\nFailed: ${stats.totalFailed}"
        } else ""

        return "Today: ${stats.todayCount} forwarded\n" +
                "Total: ${stats.totalForwarded} forwarded" +
                failInfo +
                lastInfo +
                ruleInfo
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "forwarder_service_channel"
        const val ACTION_STOP = "personal.smsforwarder.STOP_SERVICE"
        const val NOTIFICATION_REFRESH_INTERVAL = 30_000L // 30 seconds

        fun start(context: Context) {
            val intent = Intent(context, ForwarderForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ForwarderForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun isRunning(context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            @Suppress("DEPRECATION")
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (ForwarderForegroundService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }
    }
}
