package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.models.*
import com.example.utils.DoseScheduler
import com.example.utils.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object FirebaseRepository {

    private const val TAG = "FirebaseRepository"
    private const val PREFS_NAME = "med_reminder_session_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_SESSION_TOKEN = "session_token"
    private const val KEY_IS_FIRST_TIME = "is_first_time_user"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_PHONE = "user_phone"

    private var appContext: Context? = null
    private var firestoreInstance: FirebaseFirestore? = null

    /**
     * Initializes session state from SharedPreferences on app startup.
     */
    fun initSession(context: Context) {
        appContext = context.applicationContext
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return

        val savedIsLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val savedToken = prefs.getString(KEY_SESSION_TOKEN, null)
        val savedIsFirstTime = prefs.getBoolean(KEY_IS_FIRST_TIME, true)
        val savedName = prefs.getString(KEY_USER_NAME, "Sarah Jenkins") ?: "Sarah Jenkins"
        val savedPhone = prefs.getString(KEY_USER_PHONE, "+1 (555) 234-5678") ?: "+1 (555) 234-5678"

        _isLoggedIn.value = savedIsLoggedIn
        _sessionToken.value = savedToken
        _isFirstTimeUser.value = savedIsFirstTime
        _currentUser.value = _currentUser.value.copy(name = savedName, phone = savedPhone)

        Log.d(TAG, "Session restored: isLoggedIn=$savedIsLoggedIn, token=$savedToken, firstTime=$savedIsFirstTime")
    }

    private fun getFirestore(): FirebaseFirestore? {
        if (firestoreInstance == null) {
            try {
                firestoreInstance = FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore not initialized or google-services missing, using in-memory mode: ${e.message}")
            }
        }
        return firestoreInstance
    }

    // Auth & Session State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _sessionToken = MutableStateFlow<String?>(null)
    val sessionToken: StateFlow<String?> = _sessionToken.asStateFlow()

    private val _generatedOtp = MutableStateFlow<String?>(null)
    val generatedOtp: StateFlow<String?> = _generatedOtp.asStateFlow()

    private val _isFirstTimeUser = MutableStateFlow(true)
    val isFirstTimeUser: StateFlow<Boolean> = _isFirstTimeUser.asStateFlow()

    // Default Current User
    private val _currentUser = MutableStateFlow(
        User(
            id = "user_sarah_1",
            name = "Sarah Jenkins",
            phone = "+1 (555) 234-5678",
            guardian_ids = listOf("contact_daughter_1", "contact_doctor_1")
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Local State Caches (Instantly synced to UI)
    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    private val _doses = MutableStateFlow<List<Dose>>(emptyList())
    val doses: StateFlow<List<Dose>> = _doses.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        // Pre-populate with realistic initial state
        setupInitialData()
    }

    private fun setupInitialData() {
        val defaultUser = User(
            id = "user_sarah_1",
            name = "Sarah Jenkins",
            phone = "+1 (555) 234-5678",
            guardian_ids = listOf("contact_daughter_1")
        )

        val todayIso = ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val initialMeds = listOf(
            Medicine("med_1", defaultUser.id, "Amoxicillin", "500mg", "twice daily", 7, todayIso),
            Medicine("med_2", defaultUser.id, "Vitamin D3", "1000 IU", "once daily", 30, todayIso),
            Medicine("med_3", defaultUser.id, "Lisinopril", "10mg", "once daily", 14, todayIso)
        )

        val allDoses = mutableListOf<Dose>()
        val allReminders = mutableListOf<Reminder>()

        initialMeds.forEach { med ->
            val (generatedDoses, generatedReminders) = DoseScheduler.generateSchedule(med)
            allDoses.addAll(generatedDoses)
            allReminders.addAll(generatedReminders)
        }

        _medicines.value = initialMeds
        _doses.value = allDoses
        _reminders.value = allReminders

        // Attempt Firestore initial sync
        syncToFirestore(defaultUser, initialMeds, allDoses, allReminders)
    }

    private fun syncToFirestore(
        user: User,
        meds: List<Medicine>,
        dosesList: List<Dose>,
        remindersList: List<Reminder>
    ) {
        val db = getFirestore() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("users").document(user.id).set(user)

                meds.forEach { med ->
                    db.collection("medicines").document(med.id).set(med)
                }

                dosesList.forEach { dose ->
                    db.collection("doses").document(dose.id).set(dose)
                }

                remindersList.forEach { rem ->
                    db.collection("reminders").document(rem.id).set(rem)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sync to Firestore failed: ${e.message}")
            }
        }
    }

    /**
     * Adds a medicine, generates doses for duration_days, schedules local reminders,
     * and updates Firebase collections.
     */
    fun addMedicineWithAutoSchedule(
        name: String,
        dose: String,
        frequency: String,
        durationDays: Int,
        context: Context? = null
    ) {
        val user = _currentUser.value
        val todayIso = ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val medId = "med_${System.currentTimeMillis()}"

        val newMedicine = Medicine(
            id = medId,
            user_id = user.id,
            name = name,
            dose = dose,
            frequency = frequency,
            duration_days = durationDays,
            start_date = todayIso
        )

        val (newDoses, newReminders) = DoseScheduler.generateSchedule(newMedicine)

        // Update local state flows
        val updatedMeds = _medicines.value + newMedicine
        val updatedDoses = _doses.value + newDoses
        val updatedReminders = _reminders.value + newReminders

        _medicines.value = updatedMeds
        _doses.value = updatedDoses
        _reminders.value = updatedReminders

        // Schedule local notifications for each reminder
        if (context != null) {
            newReminders.forEach { reminder ->
                NotificationHelper.scheduleLocalNotification(
                    context = context,
                    reminder = reminder,
                    medicineName = name,
                    doseAmount = dose
                )
            }
        }

        // Firestore Update
        val db = getFirestore()
        if (db != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    db.collection("medicines").document(newMedicine.id).set(newMedicine)
                    newDoses.forEach { d ->
                        db.collection("doses").document(d.id).set(d)
                    }
                    newReminders.forEach { r ->
                        db.collection("reminders").document(r.id).set(r)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed writing new medicine to Firestore: ${e.message}")
                }
            }
        }
    }

    /**
     * Marks dose status as "taken", "pending", or "missed", updating Firestore and local flow.
     */
    fun markDoseStatus(doseId: String, status: DoseStatus) {
        val statusString = status.name.lowercase()

        val updatedDoses = _doses.value.map { dose ->
            if (dose.id == doseId) dose.copy(status = statusString) else dose
        }
        _doses.value = updatedDoses

        val db = getFirestore()
        if (db != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    db.collection("doses").document(doseId).update("status", statusString)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating dose status in Firestore: ${e.message}")
                }
            }
        }
    }

    /**
     * Toggles a dose status between TAKEN and PENDING
     */
    fun toggleDoseTaken(doseId: String) {
        val dose = _doses.value.find { it.id == doseId } ?: return
        val currentStatus = DoseStatus.fromString(dose.status)
        val newStatus = if (currentStatus == DoseStatus.TAKEN) DoseStatus.PENDING else DoseStatus.TAKEN
        markDoseStatus(doseId, newStatus)
    }

    /**
     * Deletes a medicine and its associated doses and reminders
     */
    fun deleteMedicine(medicineId: String) {
        _medicines.value = _medicines.value.filter { it.id != medicineId }
        val remainingDoses = _doses.value.filter { it.medicine_id != medicineId }
        val remainingDoseIds = remainingDoses.map { it.id }.toSet()
        _doses.value = remainingDoses
        _reminders.value = _reminders.value.filter { it.dose_id in remainingDoseIds }

        val db = getFirestore()
        if (db != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    db.collection("medicines").document(medicineId).delete()
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting medicine from Firestore: ${e.message}")
                }
            }
        }
    }

    /**
     * Generates a 6-digit OTP code for the given phone number and saves it in state.
     */
    fun sendOtp(phoneNumber: String): String {
        // Generate a 6-digit random code or default demo 123456
        val otpCode = (100000..999999).random().toString()
        _generatedOtp.value = otpCode

        // Update phone number for user
        val updatedUser = _currentUser.value.copy(phone = phoneNumber)
        _currentUser.value = updatedUser

        Log.d(TAG, "Generated OTP $otpCode for phone $phoneNumber")
        return otpCode
    }

    /**
     * Verifies the entered OTP code. Returns true if valid, creates a session token, and updates session state.
     */
    fun verifyOtp(phoneNumber: String, enteredOtp: String): Boolean {
        val currentOtp = _generatedOtp.value
        // Accept generated OTP or fallback 123456 for testing simplicity
        val isValid = (enteredOtp == currentOtp || enteredOtp == "123456") && enteredOtp.length == 6

        if (isValid) {
            val token = "session_token_${System.currentTimeMillis()}_${(1000..9999).random()}"
            _sessionToken.value = token
            _isLoggedIn.value = true

            // Update currentUser phone
            val updatedUser = _currentUser.value.copy(
                phone = phoneNumber.ifBlank { _currentUser.value.phone }
            )
            _currentUser.value = updatedUser

            // Persist session in SharedPreferences
            appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.apply {
                putBoolean(KEY_IS_LOGGED_IN, true)
                putString(KEY_SESSION_TOKEN, token)
                putString(KEY_USER_PHONE, updatedUser.phone)
                putString(KEY_USER_NAME, updatedUser.name)
                apply()
            }

            // Sync user session details to Firestore
            val db = getFirestore()
            if (db != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        db.collection("users").document(updatedUser.id).set(updatedUser)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed syncing user to Firestore: ${e.message}")
                    }
                }
            }
        }

        return isValid
    }

    /**
     * Completes onboarding flow
     */
    fun completeOnboarding() {
        _isFirstTimeUser.value = false
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.apply {
            putBoolean(KEY_IS_FIRST_TIME, false)
            apply()
        }
    }

    /**
     * Clears user session and logs out
     */
    fun logout() {
        _isLoggedIn.value = false
        _sessionToken.value = null
        _generatedOtp.value = null

        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            putString(KEY_SESSION_TOKEN, null)
            apply()
        }
    }
}
