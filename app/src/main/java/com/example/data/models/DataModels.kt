package com.example.data.models

data class FamilyContact(
    val id: String = "",
    val userId: String = "user_sarah_1",
    val name: String = "",
    val relation: String = "",
    val phone: String = "",
    val isPrimaryCaregiver: Boolean = false
)

data class User(
    val id: String = "user_sarah_1",
    val name: String = "Sarah Jenkins",
    val phone: String = "+1 (555) 234-5678",
    val guardian_ids: List<String> = listOf("contact_daughter_1")
)

enum class DoseStatus {
    PENDING,
    TAKEN,
    MISSED;

    companion object {
        fun fromString(value: String): DoseStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

data class Medicine(
    val id: String = "",
    val user_id: String = "user_sarah_1",
    val name: String = "",
    val dose: String = "",
    val frequency: String = "twice daily",
    val duration_days: Int = 7,
    val start_date: String = ""
)

data class Dose(
    val id: String = "",
    val medicine_id: String = "",
    val medicine_name: String = "",
    val dose_amount: String = "",
    val scheduled_time: String = "",
    val status: String = DoseStatus.PENDING.name.lowercase()
)

data class Reminder(
    val id: String = "",
    val dose_id: String = "",
    val notify_time: String = "",
    val sent: Boolean = false
)
