package com.example.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.models.Dose
import com.example.data.models.Reminder
import com.example.receiver.NotificationReceiver
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object NotificationHelper {

    const val CHANNEL_ID = "med_reminders_channel"
    const val CHANNEL_NAME = "Medication Reminders"
    const val EXTRA_DOSE_ID = "extra_dose_id"
    const val EXTRA_MED_NAME = "extra_med_name"
    const val ACTION_MARK_TAKEN = "com.example.action.MARK_TAKEN"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority medication dosage reminders for seniors"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules a local notification reminder using AlarmManager at the specified notify_time.
     */
    fun scheduleLocalNotification(
        context: Context,
        reminder: Reminder,
        medicineName: String,
        doseAmount: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.action.ALARM_TRIGGER"
            putExtra(EXTRA_DOSE_ID, reminder.dose_id)
            putExtra(EXTRA_MED_NAME, medicineName)
            putExtra("extra_dose_amount", doseAmount)
        }

        val pendingIntentId = reminder.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            pendingIntentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val notifyMillis = try {
                val zdt = ZonedDateTime.parse(reminder.notify_time, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                zdt.toInstant().toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis() + 60_000 // Fallback 1 min
            }

            // Only schedule if time is in future or near present
            if (notifyMillis > System.currentTimeMillis() - 5_000) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notifyMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Displays a rich, elderly-friendly local notification with a direct "MARK TAKEN" action button.
     * Tapping notification or button triggers marking dose taken in Firebase/Local storage.
     */
    fun showDoseReminderNotification(
        context: Context,
        doseId: String,
        medicineName: String,
        doseAmount: String
    ) {
        createNotificationChannel(context)

        // Intent on tapping the notification body -> Opens app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DOSE_ID, doseId)
            putExtra("auto_mark_taken", true)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            doseId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Direct Action: Mark Taken
        val markTakenIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_MARK_TAKEN
            putExtra(EXTRA_DOSE_ID, doseId)
        }

        val markTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            doseId.hashCode() + 99,
            markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ Time to take $medicineName")
            .setContentText("Dose: $doseAmount. Tap to mark as taken.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Time to take $medicineName ($doseAmount). Keeping your schedule on track helps maintain your health!"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "MARK TAKEN",
                markTakenPendingIntent
            )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(doseId.hashCode(), builder.build())
    }
}
