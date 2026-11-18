package com.aloxe.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.aloxe.app.data.MatchedMessage
import com.aloxe.app.data.MessageStore
import com.aloxe.app.data.Prefs
import com.aloxe.app.ui.AlertActivity
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lắng nghe thông báo hệ thống. KHÔNG đọc toàn bộ nội dung Zalo, chỉ nhìn vào
 * thông báo mà Zalo đã tự hiển thị ra — giống như một người ngồi nhìn màn hình
 * thông báo thay bạn, không đăng nhập hay giả danh tài khoản Zalo nào cả.
 */
class ZaloNotificationListener : NotificationListenerService() {

    companion object {
        // Gói ứng dụng Zalo chính thức trên Google Play.
        private val ZALO_PACKAGES = setOf("com.zing.zalo")

        private const val CHANNEL_ID = "ride_alerts"
        private val notificationIdCounter = AtomicInteger(1000)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        ensureChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (sbn.packageName !in ZALO_PACKAGES) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val content = if (bigText.isNotBlank()) bigText else text

        if (title.isBlank() || content.isBlank()) return

        val prefs = Prefs(applicationContext)
        prefs.rememberGroup(title)

        if (!prefs.shouldMonitorGroup(title)) return

        val include = prefs.getIncludeKeywords()
        val exclude = prefs.getExcludeKeywords()
        if (!KeywordMatcher.matches(content, include, exclude)) return

        val message = MatchedMessage(
            groupTitle = title,
            content = content,
            timestampMillis = System.currentTimeMillis()
        )
        MessageStore(applicationContext).add(message)

        triggerFullScreenAlert(message)
    }

    private fun triggerFullScreenAlert(message: MatchedMessage) {
        val fullScreenIntent = Intent(this, AlertActivity::class.java).apply {
            putExtra(AlertActivity.EXTRA_GROUP_TITLE, message.groupTitle)
            putExtra(AlertActivity.EXTRA_CONTENT, message.content)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val requestCode = notificationIdCounter.incrementAndGet()
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, requestCode, fullScreenIntent, pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Có khách đặt xe: ${message.groupTitle}")
            .setContentText(message.content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(requestCode, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Báo cuốc xe",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Thông báo khi có khách đặt xe khớp từ khóa trong nhóm Zalo"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
            setBypassDnd(true)
        }
        nm.createNotificationChannel(channel)
    }
}
