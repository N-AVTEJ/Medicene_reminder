package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToMedicines: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var isMorningDoseTaken by remember { mutableStateOf(true) }
    var isNoonDoseTaken by remember { mutableStateOf(false) }
    var isEveningDoseTaken by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Profile Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_header_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Good Morning, Sarah 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You have 2 remaining doses scheduled today.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "User Profile",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Quick Actions Row - Big Tappable Buttons
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Scan Prescription Banner Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToScan() }
                        .testTag("action_scan_prescription"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Rx",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Scan Label",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Prescription AI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Add Medicine Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToMedicines() }
                        .testTag("action_my_medicines"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "My Medicines",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "My Medicines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "3 Pills Active",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Today's Dose Schedule Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Medication",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onNavigateToReminders,
                    modifier = Modifier.testTag("view_all_reminders")
                ) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Dose Card 1
        item {
            DoseScheduleItemCard(
                time = "08:00 AM",
                medicineName = "Amoxicillin 500mg",
                instructions = "1 Capsule • Take with water after breakfast",
                isTaken = isMorningDoseTaken,
                onToggle = { isMorningDoseTaken = !isMorningDoseTaken },
                tag = "dose_morning"
            )
        }

        // Dose Card 2
        item {
            DoseScheduleItemCard(
                time = "01:00 PM",
                medicineName = "Vitamin D3 1000 IU",
                instructions = "1 Tablet • Take after lunch",
                isTaken = isNoonDoseTaken,
                onToggle = { isNoonDoseTaken = !isNoonDoseTaken },
                tag = "dose_noon"
            )
        }

        // Dose Card 3
        item {
            DoseScheduleItemCard(
                time = "08:00 PM",
                medicineName = "Lisinopril 10mg",
                instructions = "1 Tablet • Take before bedtime",
                isTaken = isEveningDoseTaken,
                onToggle = { isEveningDoseTaken = !isEveningDoseTaken },
                tag = "dose_evening"
            )
        }
    }
}

@Composable
fun DoseScheduleItemCard(
    time: String,
    medicineName: String,
    instructions: String,
    isTaken: Boolean,
    onToggle: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Big Elderly-Friendly Action Button
                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTaken) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                        contentColor = if (isTaken) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("${tag}_checkbox"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isTaken) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTaken) "TAKEN" else "TAKE DOSE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = medicineName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = instructions,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

