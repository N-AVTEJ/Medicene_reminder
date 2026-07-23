package com.example.utils

import com.example.data.models.Dose
import com.example.data.models.DoseStatus
import com.example.data.models.Medicine
import com.example.data.models.Reminder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object DoseScheduler {

    private val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    /**
     * Parses frequency string and returns standard local times for doses.
     * e.g. "twice daily" -> [08:00, 20:00]
     */
    fun parseFrequencyToTimeSlots(frequency: String): List<LocalTime> {
        val freqLower = frequency.lowercase().trim()
        return when {
            freqLower.contains("once") || freqLower.contains("1 time") || freqLower.contains("1x") -> {
                listOf(LocalTime.of(8, 0))
            }
            freqLower.contains("twice") || freqLower.contains("2 times") || freqLower.contains("2x") || freqLower.contains("2 times daily") -> {
                listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))
            }
            freqLower.contains("three") || freqLower.contains("3 times") || freqLower.contains("3x") -> {
                listOf(LocalTime.of(8, 0), LocalTime.of(14, 0), LocalTime.of(20, 0))
            }
            freqLower.contains("four") || freqLower.contains("4 times") || freqLower.contains("4x") -> {
                listOf(LocalTime.of(8, 0), LocalTime.of(12, 0), LocalTime.of(16, 0), LocalTime.of(20, 0))
            }
            else -> {
                // Default to twice daily if ambiguous
                listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))
            }
        }
    }

    /**
     * Auto-generates dose schedule and reminder objects for a medicine based on frequency
     * and duration_days, explicitly anchored to device locale system timezone.
     */
    fun generateSchedule(
        medicine: Medicine,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Pair<List<Dose>, List<Reminder>> {
        val startDate = try {
            if (medicine.start_date.isNotBlank()) {
                LocalDate.parse(medicine.start_date.take(10))
            } else {
                LocalDate.now(zoneId)
            }
        } catch (e: Exception) {
            LocalDate.now(zoneId)
        }

        val timeSlots = parseFrequencyToTimeSlots(medicine.frequency)
        val durationDays = if (medicine.duration_days > 0) medicine.duration_days else 7

        val generatedDoses = mutableListOf<Dose>()
        val generatedReminders = mutableListOf<Reminder>()

        val medId = if (medicine.id.isNotBlank()) medicine.id else UUID.randomUUID().toString()

        for (dayOffset in 0 until durationDays) {
            val currentDay = startDate.plusDays(dayOffset.toLong())

            for (timeSlot in timeSlots) {
                val localDateTime = LocalDateTime.of(currentDay, timeSlot)
                val zonedDateTime = ZonedDateTime.of(localDateTime, zoneId)
                val isoScheduledTime = zonedDateTime.format(isoFormatter)

                val doseId = "dose_${medId}_day${dayOffset}_${timeSlot.hour}"
                val reminderId = "rem_${doseId}"

                val dose = Dose(
                    id = doseId,
                    medicine_id = medId,
                    medicine_name = medicine.name,
                    dose_amount = medicine.dose,
                    scheduled_time = isoScheduledTime,
                    status = DoseStatus.PENDING.name.lowercase()
                )

                val reminder = Reminder(
                    id = reminderId,
                    dose_id = doseId,
                    notify_time = isoScheduledTime,
                    sent = false
                )

                generatedDoses.add(dose)
                generatedReminders.add(reminder)
            }
        }

        return Pair(generatedDoses, generatedReminders)
    }

    fun formatDisplayTime(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString, isoFormatter)
            zdt.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } catch (e: Exception) {
            isoString
        }
    }

    fun formatDisplayDate(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString, isoFormatter)
            zdt.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
        } catch (e: Exception) {
            isoString
        }
    }
}
