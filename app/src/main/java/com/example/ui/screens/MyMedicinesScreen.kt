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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    var medicines by remember {
        mutableStateOf(
            listOf(
                MedicineItem("1", "Amoxicillin", "500mg Capsule", "Twice Daily", 14, "Prescription"),
                MedicineItem("2", "Lisinopril", "10mg Tablet", "Once Daily (Evening)", 28, "Prescription"),
                MedicineItem("3", "Vitamin D3", "1000 IU Tablet", "Daily (Morning)", 5, "Supplements")
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newDosage by remember { mutableStateOf("") }
    var newFrequency by remember { mutableStateOf("Once Daily") }
    var newPills by remember { mutableStateOf("30") }

    val filteredList = when (selectedFilter) {
        "Prescriptions" -> medicines.filter { it.category == "Prescription" }
        "Supplements" -> medicines.filter { it.category == "Supplements" }
        else -> medicines
    }

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
                text = "My Medicines",
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
                items(filteredList, key = { it.id }) { med ->
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
                                        text = "${med.dosage} • ${med.frequency}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (med.remainingPills <= 7) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Refill warning",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "LOW STOCK: ${med.remainingPills} LEFT",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "${med.remainingPills} pills remaining in bottle",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = {
                                    medicines = medicines.filter { it.id != med.id }
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
                    text = "Add New Medicine",
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
                        label = { Text("Dosage (e.g., 250mg)", style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_med_dosage")
                    )
                    OutlinedTextField(
                        value = newFrequency,
                        onValueChange = { newFrequency = it },
                        label = { Text("Frequency (e.g., Once Daily)", style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_med_frequency")
                    )
                    OutlinedTextField(
                        value = newPills,
                        onValueChange = { newPills = it },
                        label = { Text("Total Pill Count", style = MaterialTheme.typography.bodyLarge) },
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
                            val newMed = MedicineItem(
                                id = System.currentTimeMillis().toString(),
                                name = newName,
                                dosage = if (newDosage.isBlank()) "500mg" else newDosage,
                                frequency = newFrequency,
                                remainingPills = newPills.toIntOrNull() ?: 30,
                                category = "Prescription"
                            )
                            medicines = medicines + newMed
                            newName = ""
                            newDosage = ""
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("submit_new_medicine")
                ) {
                    Text("Save Medicine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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

