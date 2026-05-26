package com.gymtracker.app.utils

import android.content.Context
import androidx.work.*
import com.gymtracker.app.worker.ReminderWorker
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(context: Context, hour: Int, minute: Int) {
        val delay = millisUntilNextOccurrence(hour, minute)
        enqueueOnce(context, delay)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(ReminderWorker.WORK_TAG)
    }

    fun enqueueOnce(context: Context, delayMillis: Long) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(ReminderWorker.WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ReminderWorker.WORK_TAG,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun millisUntilNextOccurrence(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        val todayTarget = now.toLocalDate().atTime(LocalTime.of(hour, minute))
        val next = if (now.isBefore(todayTarget)) todayTarget else todayTarget.plusDays(1)
        return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                System.currentTimeMillis()
    }
}
