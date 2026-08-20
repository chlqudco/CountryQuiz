package com.chlqudco.countryquiz.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.chlqudco.countryquiz.MainActivity
import com.chlqudco.countryquiz.R
import com.chlqudco.countryquiz.data.ProgressStore
import java.time.ZonedDateTime

object ReviewReminderScheduler {
    const val ACTION_REVIEW_REMINDER = "com.chlqudco.countryquiz.REVIEW_REMINDER"
    private const val REQUEST_CODE = 3109

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextReminderAt(),
            reminderIntent(context)
        )
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(reminderIntent(context))
    }

    private fun nextReminderAt(now: ZonedDateTime = ZonedDateTime.now()): Long {
        var next = now.withHour(9).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return next.toInstant().toEpochMilli()
    }

    private fun reminderIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ReviewReminderReceiver::class.java).setAction(ACTION_REVIEW_REMINDER),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

class ReviewReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val store = ProgressStore(context)
        val progress = store.load()
        if (!progress.settings.reviewNotificationsEnabled) {
            ReviewReminderScheduler.cancel(context)
            return
        }
        if (intent.action == ReviewReminderScheduler.ACTION_REVIEW_REMINDER) {
            val dueCount = store.dueReviewCount(progress)
            if (dueCount > 0) notifyReview(context, dueCount)
        }
        ReviewReminderScheduler.schedule(context)
    }

    private fun notifyReview(context: Context, dueCount: Int) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "복습 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("오늘 복습할 문제가 있어요")
            .setContentText("잊기 전에 ${dueCount}개의 국가·수도 연결을 확인해 보세요")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val CHANNEL_ID = "review_reminders"
        const val NOTIFICATION_ID = 3109
    }
}
