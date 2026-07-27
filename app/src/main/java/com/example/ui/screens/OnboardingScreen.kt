package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.FirebaseRepository

data class CountryOption(val code: String, val name: String, val flag: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onCompleteOnboarding: () -> Unit
) {
    var onboardingStep by remember { mutableIntStateOf(1) } // 1: Primary Contact (Mandatory), 2: Backup Contact (Optional)

    // Primary Caregiver State
    var primaryName by remember { mutableStateOf("") }
    var primaryRelationship by remember { mutableStateOf("") }
    var primaryPhone by remember { mutableStateOf("") }
    var primaryCountryCode by remember { mutableStateOf("+1") }
    var primaryCountryDropdownExpanded by remember { mutableStateOf(false) }
    var primaryRelationshipDropdownExpanded by remember { mutableStateOf(false) }

    // Backup Caregiver State (Optional)
    var backupName by remember { mutableStateOf("") }
    var backupRelationship by remember { mutableStateOf("") }
    var backupPhone by remember { mutableStateOf("") }
    var backupCountryCode by remember { mutableStateOf("+1") }
    var backupCountryDropdownExpanded by remember { mutableStateOf(false) }
    var backupRelationshipDropdownExpanded by remember { mutableStateOf(false) }

    val countryOptions = remember {
        listOf(
            CountryOption("+1", "US/CA", "🇺🇸"),
            CountryOption("+44", "UK", "🇬🇧"),
            CountryOption("+91", "India", "🇮🇳"),
            CountryOption("+61", "Australia", "🇦🇺"),
            CountryOption("+49", "Germany", "🇩🇪"),
            CountryOption("+81", "Japan", "🇯🇵")
        )
    }

    val relationshipOptions = listOf("Son", "Daughter", "Other")

    // Validation logic for Primary Contact
    val isPrimaryNameValid = primaryName.trim().isNotBlank()
    val isPrimaryRelationshipValid = primaryRelationship.trim().isNotBlank() && relationshipOptions.contains(primaryRelationship.trim())
    val primaryPhoneDigits = primaryPhone.filter { it.isDigit() }
    val isPrimaryPhoneValid = primaryPhoneDigits.length == 10
    val primaryPhoneHasError = primaryPhone.isNotEmpty() && !isPrimaryPhoneValid
    val isPrimaryFormValid = isPrimaryNameValid && isPrimaryRelationshipValid && isPrimaryPhoneValid

    // Validation logic for Backup Contact
    val isBackupEmpty = backupName.trim().isEmpty() && backupRelationship.trim().isEmpty() && backupPhone.trim().isEmpty()
    val isBackupNameValid = backupName.trim().isNotBlank()
    val isBackupRelationshipValid = backupRelationship.trim().isNotBlank() && relationshipOptions.contains(backupRelationship.trim())
    val backupPhoneDigits = backupPhone.filter { it.isDigit() }
    val isBackupPhoneValid = backupPhoneDigits.length == 10
    val backupPhoneHasError = backupPhone.isNotEmpty() && !isBackupPhoneValid
    val isBackupFormValid = isBackupEmpty || (isBackupNameValid && isBackupRelationshipValid && isBackupPhoneValid)

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Header Icon & Step Indicator
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (onboardingStep == 1) "Add Primary Family Contact" else "Add Backup Contact (Optional)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (onboardingStep == 1)
                        "Step 1 of 2 • Required for emergency missed-dose alerts"
                    else
                        "Step 2 of 2 • Optional fallback contact if primary caregiver is unreachable",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (onboardingStep == 1) {
                    // STEP 1: Primary Contact
                    // Field 1: Name
                    OutlinedTextField(
                        value = primaryName,
                        onValueChange = { primaryName = it },
                        label = { Text("Name", style = MaterialTheme.typography.bodyLarge) },
                        placeholder = { Text("e.g. David Miller") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp))
                        },
                        textStyle = MaterialTheme.typography.titleMedium,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("caregiver_name_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 2: Relationship Dropdown
                    ExposedDropdownMenuBox(
                        expanded = primaryRelationshipDropdownExpanded,
                        onExpandedChange = { primaryRelationshipDropdownExpanded = !primaryRelationshipDropdownExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("caregiver_relationship_box")
                    ) {
                        OutlinedTextField(
                            value = if (primaryRelationship.isEmpty()) "Select Relationship (Son/Daughter/Other)" else primaryRelationship,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Relationship", style = MaterialTheme.typography.bodyLarge) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = primaryRelationshipDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("caregiver_relationship_input")
                        )

                        ExposedDropdownMenu(
                            expanded = primaryRelationshipDropdownExpanded,
                            onDismissRequest = { primaryRelationshipDropdownExpanded = false }
                        ) {
                            relationshipOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        primaryRelationship = option
                                        primaryRelationshipDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("relationship_option_$option")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 3: Country Code + Phone Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = primaryCountryDropdownExpanded,
                            onExpandedChange = { primaryCountryDropdownExpanded = !primaryCountryDropdownExpanded },
                            modifier = Modifier
                                .width(115.dp)
                                .testTag("country_code_box")
                        ) {
                            OutlinedTextField(
                                value = primaryCountryCode,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Code") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = primaryCountryDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("country_code_input")
                            )

                            ExposedDropdownMenu(
                                expanded = primaryCountryDropdownExpanded,
                                onDismissRequest = { primaryCountryDropdownExpanded = false }
                            ) {
                                countryOptions.forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text("${country.flag} ${country.code} (${country.name})") },
                                        onClick = {
                                            primaryCountryCode = country.code
                                            primaryCountryDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("country_option_${country.code}")
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = primaryPhone,
                                onValueChange = { newValue ->
                                    val digitsOnly = newValue.filter { it.isDigit() }
                                    if (digitsOnly.length <= 10) {
                                        primaryPhone = digitsOnly
                                    }
                                },
                                label = { Text("Phone (10 digits)") },
                                placeholder = { Text("5550192831") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(22.dp))
                                },
                                isError = primaryPhoneHasError,
                                textStyle = MaterialTheme.typography.titleMedium,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("caregiver_phone_input")
                            )
                        }
                    }

                    if (primaryPhoneHasError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ Phone number must be exactly 10 digits (${primaryPhoneDigits.length}/10 entered)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_validation_error_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isPrimaryFormValid) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("caregiver_validation_banner")
                        ) {
                            Text(
                                text = when {
                                    !isPrimaryNameValid -> "⚠️ Primary Caregiver Name is required."
                                    !isPrimaryRelationshipValid -> "⚠️ Relationship must be selected (Son, Daughter, or Other)."
                                    !isPrimaryPhoneValid -> "⚠️ Caregiver phone number must be exactly 10 digits."
                                    else -> "⚠️ Please complete all fields to continue."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("caregiver_valid_banner")
                        ) {
                            Text(
                                text = "✅ Primary contact valid: $primaryCountryCode $primaryPhone ($primaryName).",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                } else {
                    // STEP 2: Backup Contact (Optional & Skippable)
                    // Field 1: Backup Name
                    OutlinedTextField(
                        value = backupName,
                        onValueChange = { backupName = it },
                        label = { Text("Backup Contact Name", style = MaterialTheme.typography.bodyLarge) },
                        placeholder = { Text("e.g. Dr. Robert Vance") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp))
                        },
                        textStyle = MaterialTheme.typography.titleMedium,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_caregiver_name_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 2: Backup Relationship Dropdown
                    ExposedDropdownMenuBox(
                        expanded = backupRelationshipDropdownExpanded,
                        onExpandedChange = { backupRelationshipDropdownExpanded = !backupRelationshipDropdownExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_caregiver_relationship_box")
                    ) {
                        OutlinedTextField(
                            value = if (backupRelationship.isEmpty()) "Select Relationship (Son/Daughter/Other)" else backupRelationship,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Relationship", style = MaterialTheme.typography.bodyLarge) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = backupRelationshipDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("backup_caregiver_relationship_input")
                        )

                        ExposedDropdownMenu(
                            expanded = backupRelationshipDropdownExpanded,
                            onDismissRequest = { backupRelationshipDropdownExpanded = false }
                        ) {
                            relationshipOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        backupRelationship = option
                                        backupRelationshipDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("backup_relationship_option_$option")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 3: Backup Country Code + Phone Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = backupCountryDropdownExpanded,
                            onExpandedChange = { backupCountryDropdownExpanded = !backupCountryDropdownExpanded },
                            modifier = Modifier
                                .width(115.dp)
                                .testTag("backup_country_code_box")
                        ) {
                            OutlinedTextField(
                                value = backupCountryCode,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Code") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = backupCountryDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("backup_country_code_input")
                            )

                            ExposedDropdownMenu(
                                expanded = backupCountryDropdownExpanded,
                                onDismissRequest = { backupCountryDropdownExpanded = false }
                            ) {
                                countryOptions.forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text("${country.flag} ${country.code} (${country.name})") },
                                        onClick = {
                                            backupCountryCode = country.code
                                            backupCountryDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("backup_country_option_${country.code}")
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = backupPhone,
                                onValueChange = { newValue ->
                                    val digitsOnly = newValue.filter { it.isDigit() }
                                    if (digitsOnly.length <= 10) {
                                        backupPhone = digitsOnly
                                    }
                                },
                                label = { Text("Phone (10 digits)") },
                                placeholder = { Text("5550192831") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(22.dp))
                                },
                                isError = backupPhoneHasError,
                                textStyle = MaterialTheme.typography.titleMedium,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("backup_caregiver_phone_input")
                            )
                        }
                    }

                    if (backupPhoneHasError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ Backup phone number must be 10 digits (${backupPhoneDigits.length}/10 entered)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("backup_phone_validation_error_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isBackupFormValid) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("backup_caregiver_validation_banner")
                        ) {
                            Text(
                                text = "⚠️ If adding a backup contact, Name, Relationship, and 10-digit Phone are required.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else if (!isBackupEmpty) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("backup_caregiver_valid_banner")
                        ) {
                            Text(
                                text = "✅ Backup contact valid: $backupCountryCode $backupPhone ($backupName).",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Action
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (onboardingStep == 1) {
                    Button(
                        onClick = {
                            if (isPrimaryFormValid) {
                                onboardingStep = 2
                            }
                        },
                        enabled = isPrimaryFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("caregiver_continue_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Continue to Backup Contact",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (isBackupFormValid) {
                                FirebaseRepository.saveOnboardingCaregivers(
                                    primaryName = primaryName,
                                    primaryRelation = primaryRelationship,
                                    primaryPhone = "$primaryCountryCode $primaryPhone",
                                    backupName = if (backupName.isNotBlank()) backupName else null,
                                    backupRelation = backupRelationship,
                                    backupPhone = if (backupPhone.isNotBlank()) "$backupCountryCode $backupPhone" else null
                                )
                                FirebaseRepository.completeOnboarding()
                                onCompleteOnboarding()
                            }
                        },
                        enabled = isBackupFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("backup_caregiver_continue_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (isBackupEmpty) "Complete Setup" else "Save Backup & Complete Setup",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            FirebaseRepository.saveOnboardingCaregivers(
                                primaryName = primaryName,
                                primaryRelation = primaryRelationship,
                                primaryPhone = "$primaryCountryCode $primaryPhone",
                                backupName = null,
                                backupRelation = null,
                                backupPhone = null
                            )
                            FirebaseRepository.completeOnboarding()
                            onCompleteOnboarding()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("skip_backup_contact_button")
                    ) {
                        Text(
                            text = "Skip for Now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}


