package com.example

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
import com.example.ui.components.MedReminderBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MedReminderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedReminderTheme {
                MedReminderApp()
            }
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
                        onLoginSuccess = { currentScreen = Screen.Home }
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
