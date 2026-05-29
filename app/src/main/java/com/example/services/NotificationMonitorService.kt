package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.SocialTaskerApp
import com.example.data.SocialMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationMonitorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val TAG = "NotificationMonitor"

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Monitor Service Connected!")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        val isInstagram = packageName == "com.instagram.android"
        val isSnapchat = packageName == "com.snapchat.android"

        if (isInstagram || isSnapchat) {
            val extras = sbn.notification?.extras ?: return
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "Someone"
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "sent you a message"
            val platform = if (isInstagram) "Instagram" else "Snapchat"

            Log.d(TAG, "Received notification from $platform: $title -> $text")

            // Persist the message details into the local Room database
            saveSocialMessage(title, text, platform)

            // Trigger standard system notification alerting the user about the custom tasker alert
            showInAppSystemNotification(title, text, platform)
        }
    }

    private fun saveSocialMessage(title: String, text: String, platform: String) {
        val repository = (applicationContext as? SocialTaskerApp)?.repository ?: return
        serviceScope.launch {
            val message = SocialMessage(
                sender = title,
                text = text,
                platform = platform,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            repository.insertMessage(message)
        }
    }

    private fun showInAppSystemNotification(sender: String, messageText: String, platform: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "social_tasker_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Social Tasker Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Custom reminders for incoming Instagram and Snapchat messages"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Social Reminder ($platform)")
            .setContentText("$sender: $messageText")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
