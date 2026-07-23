package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.FirebaseRepository

data class MedicineItem(
    val id: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val remainingPills: Int,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMedicinesScreen() {
    val context = LocalContext.current
    val repositoryMeds by FirebaseRepository.medicines.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newDosage by remember { mutableStateOf("") }
    var newFrequency by remember { mutableStateOf("Twice Daily") }
    var newDurationDays by remember { mutableStateOf("14") }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp)) },
                text = { Text("Add Medicine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .height(58.dp)
                    .testTag("add_medicine_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "My Medicines & Firebase DB",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Filter Chips with enlarged touch targets
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("All", "Prescriptions", "Supplements").forEach { category ->
                    FilterChip(
                        selected = selectedFilter == category,
                        onClick = { selectedFilter = category },
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selectedFilter == category) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("filter_$category")
                    )
                }
            }

            // Medicine List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(repositoryMeds, key = { it.id }) { med ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_item_${med.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Medication,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = med.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${med.dose} • ${med.frequency}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Duration: ${med.duration_days} days • Auto Schedule active",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    FirebaseRepository.deleteMedicine(med.id)
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("delete_med_${med.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Medicine",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Medicine Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Medicine & Auto-Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Medicine Name", style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_med_name")
                    )
                    OutlinedTextField(
                        value = newDosage,
                        onValueChange = { newDosage = it },
                        label = { Text("Dosage (e.g., 500mg)", style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_med_dosage")
                    )
                    OutlinedTextField(
                        value = newFrequency,
                        onValueChange = { newFrequency = it },
                        label = { Text("Frequency (e.g., Twice Daily)", style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_med_frequency")
                    )
                    OutlinedTextField(
                        value = newDurationDays,
                        onValueChange = { newDurationDays = it },
                        label = { Text("Duration (Days, e.g. 14)", style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_med_count")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val durationInt = newDurationDays.toIntOrNull() ?: 14
                            FirebaseRepository.addMedicineWithAutoSchedule(
                                name = newName,
                                dose = if (newDosage.isBlank()) "1 Tablet" else newDosage,
                                frequency = newFrequency,
                                durationDays = durationInt,
                                context = context
                            )
                            newName = ""
                            newDosage = ""
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("submit_new_medicine")
                ) {
                    Text("Save & Auto-Generate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Cancel", style = MaterialTheme.typography.titleMedium)
                }
            }
        )
    }
}

