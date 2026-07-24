package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Medicines : Screen("medicines", "Medicines", Icons.Filled.Medication, Icons.Outlined.Medication)
    object Reminders : Screen("reminders", "Reminders", Icons.Filled.Alarm, Icons.Outlined.Alarm)
    object FamilyContacts : Screen("contacts", "Family", Icons.Filled.ContactPhone, Icons.Outlined.ContactPhone)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object Login : Screen("login", "Login", Icons.Filled.Person, Icons.Outlined.Person)
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Filled.Person, Icons.Outlined.Person)
    object ScanPrescription : Screen("scan_prescription", "Scan RX", Icons.Filled.CameraAlt, Icons.Filled.CameraAlt)

    companion object {
        val bottomNavItems = listOf(Home, Medicines, Reminders, FamilyContacts, Settings)
    }
}
