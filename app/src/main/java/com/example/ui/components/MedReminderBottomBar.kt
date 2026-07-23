package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.Screen

@Composable
fun MedReminderBottomBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("med_reminder_bottom_navigation")
    ) {
        Screen.bottomNavItems.forEach { screen ->
            val isSelected = currentScreen.route == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                modifier = Modifier.testTag("nav_item_${screen.route}")
            )
        }
    }
}

