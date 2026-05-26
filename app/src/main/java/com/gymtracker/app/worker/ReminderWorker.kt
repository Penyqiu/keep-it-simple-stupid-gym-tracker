package com.gymtracker.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.gymtracker.app.MainActivity
import com.gymtracker.app.R
import com.gymtracker.app.data.datastore.SettingsDataStore
import com.gymtracker.app.utils.ReminderScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val enabled = settingsDataStore.reminderEnabled.first()
        if (!enabled) return Result.success()

        val days = settingsDataStore.reminderDays.first()
        val todayDayOfWeek = LocalDate.now().dayOfWeek.value // 1=Mon, 7=Sun

        if (todayDayOfWeek in days) {
            postNotification()
        }

        scheduleNext()
        return Result.success()
    }

    private fun postNotification() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(manager)

        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(manager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }

    private suspend fun scheduleNext() {
        val hour = settingsDataStore.reminderHour.first()
        val minute = settingsDataStore.reminderMinute.first()
        val delay = millisUntilTomorrow(hour, minute)
        ReminderScheduler.enqueueOnce(context, delay)
    }

    companion object {
        const val CHANNEL_ID = "workout_reminders"
        const val NOTIFICATION_ID = 2001
        const val WORK_TAG = "workout_reminder"

        fun millisUntilTomorrow(hour: Int, minute: Int): Long {
            val now = LocalDateTime.now()
            val nextRun = now.toLocalDate().plusDays(1)
                .atTime(LocalTime.of(hour, minute))
            return nextRun.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                    System.currentTimeMillis()
        }
    }
}
