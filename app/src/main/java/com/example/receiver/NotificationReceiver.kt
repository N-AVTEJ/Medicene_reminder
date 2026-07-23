package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.models.DoseStatus
import com.example.data.repository.FirebaseRepository
import com.example.utils.NotificationHelper

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val doseId = intent.getStringExtra(NotificationHelper.EXTRA_DOSE_ID) ?: return

        when (action) {
            NotificationHelper.ACTION_MARK_TAKEN -> {
                // Mark dose taken in Firebase and local state
                FirebaseRepository.markDoseStatus(doseId, DoseStatus.TAKEN)

                // Cancel notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(doseId.hashCode())
            }
            "com.example.action.ALARM_TRIGGER" -> {
                val medName = intent.getStringExtra(NotificationHelper.EXTRA_MED_NAME) ?: "Medication"
                val doseAmount = intent.getStringExtra("extra_dose_amount") ?: "1 Dose"

                NotificationHelper.showDoseReminderNotification(
                    context = context,
                    doseId = doseId,
                    medicineName = medName,
                    doseAmount = doseAmount
                )
            }
        }
    }
}
