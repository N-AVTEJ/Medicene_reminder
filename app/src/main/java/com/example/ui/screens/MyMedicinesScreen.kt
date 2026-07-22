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
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Medicine") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_medicine_fab")
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Prescriptions", "Supplements").forEach { category ->
                    FilterChip(
                        selected = selectedFilter == category,
                        onClick = { selectedFilter = category },
                        label = { Text(category) },
                        modifier = Modifier.testTag("filter_$category")
                    )
                }
            }

            // Medicine List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Medication,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = med.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${med.dosage} • ${med.frequency}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (med.remainingPills <= 7) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Refill warning",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Low stock: ${med.remainingPills} left",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "${med.remainingPills} pills remaining",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = {
                                    medicines = medicines.filter { it.id != med.id }
                                },
                                modifier = Modifier.testTag("delete_med_${med.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Medicine",
                                    tint = MaterialTheme.colorScheme.outline
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
            title = { Text("Add New Medicine") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Medicine Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_med_name")
                    )
                    OutlinedTextField(
                        value = newDosage,
                        onValueChange = { newDosage = it },
                        label = { Text("Dosage (e.g., 250mg)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_med_dosage")
                    )
                    OutlinedTextField(
                        value = newFrequency,
                        onValueChange = { newFrequency = it },
                        label = { Text("Frequency (e.g., Once Daily)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_med_frequency")
                    )
                    OutlinedTextField(
                        value = newPills,
                        onValueChange = { newPills = it },
                        label = { Text("Total Pill Count") },
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
                    modifier = Modifier.testTag("submit_new_medicine")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
