package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.models.DoseStatus
import com.example.data.repository.FirebaseRepository
import com.example.ui.components.MedReminderBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MedReminderTheme
import com.example.utils.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Process notification intent if opened from a notification tap
        handleNotificationIntent(intent)

        setContent {
            MedReminderTheme {
                MedReminderApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val doseId = intent.getStringExtra(NotificationHelper.EXTRA_DOSE_ID)
        val autoMarkTaken = intent.getBooleanExtra("auto_mark_taken", false)

        if (!doseId.isNullOrBlank() && autoMarkTaken) {
            FirebaseRepository.markDoseStatus(doseId, DoseStatus.TAKEN)
        }
    }
}

@Composable
fun MedReminderApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val isBottomBarVisible = currentScreen in Screen.bottomNavItems

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("med_reminder_main_scaffold"),
        bottomBar = {
            if (isBottomBarVisible) {
                MedReminderBottomBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        currentScreen = screen
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentScreen,
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.Home -> HomeScreen(
                        onNavigateToScan = { currentScreen = Screen.ScanPrescription },
                        onNavigateToMedicines = { currentScreen = Screen.Medicines },
                        onNavigateToReminders = { currentScreen = Screen.Reminders },
                        onNavigateToLogin = { currentScreen = Screen.Login }
                    )
                    Screen.Medicines -> MyMedicinesScreen()
                    Screen.Reminders -> RemindersScreen()
                    Screen.FamilyContacts -> FamilyContactsScreen()
                    Screen.Settings -> SettingsScreen(
                        onNavigateToLogin = { currentScreen = Screen.Login }
                    )
                    Screen.Login -> LoginScreen(
                        onNavigateBack = { currentScreen = Screen.Home },
                        onLoginSuccess = { currentScreen = Screen.Onboarding }
                    )
                    Screen.Onboarding -> OnboardingScreen(
                        onCompleteOnboarding = { currentScreen = Screen.Home }
                    )
                    Screen.ScanPrescription -> ScanPrescriptionScreen(
                        onNavigateBack = { currentScreen = Screen.Home },
                        onSaveScannedMedicine = { _ ->
                            currentScreen = Screen.Medicines
                        }
                    )
                }
            }
        }
    }
}
