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
    val currentUser by FirebaseRepository.currentUser.collectAsStateWithLifecycle()

    var contactName by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    // Country Code State
    var selectedCountryCode by remember { mutableStateOf("+1") }
    var countryDropdownExpanded by remember { mutableStateOf(false) }

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

    var relationshipDropdownExpanded by remember { mutableStateOf(false) }
    val relationshipOptions = listOf("Son", "Daughter", "Other")

    // Validation logic
    val isNameValid = contactName.trim().isNotBlank()
    val isRelationshipValid = relationship.trim().isNotBlank() && relationshipOptions.contains(relationship.trim())
    
    val phoneDigits = contactPhone.filter { it.isDigit() }
    val isPhoneValid = phoneDigits.length == 10
    val isPhoneTouched = contactPhone.isNotEmpty()
    val phoneHasError = isPhoneTouched && !isPhoneValid

    val isFormValid = isNameValid && isRelationshipValid && isPhoneValid

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
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Add Family Contact",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "A primary family contact is required to receive missed dose alerts for patient safety.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Field 1: Name
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
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

                // Field 2: Relationship Dropdown (Son, Daughter, Other)
                ExposedDropdownMenuBox(
                    expanded = relationshipDropdownExpanded,
                    onExpandedChange = { relationshipDropdownExpanded = !relationshipDropdownExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("caregiver_relationship_box")
                ) {
                    OutlinedTextField(
                        value = if (relationship.isEmpty()) "Select Relationship (Son/Daughter/Other)" else relationship,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Relationship", style = MaterialTheme.typography.bodyLarge) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("caregiver_relationship_input")
                    )

                    ExposedDropdownMenu(
                        expanded = relationshipDropdownExpanded,
                        onDismissRequest = { relationshipDropdownExpanded = false }
                    ) {
                        relationshipOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    relationship = option
                                    relationshipDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("relationship_option_$option")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Field 3: Country Code + Phone Number Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Country Code Selector
                    ExposedDropdownMenuBox(
                        expanded = countryDropdownExpanded,
                        onExpandedChange = { countryDropdownExpanded = !countryDropdownExpanded },
                        modifier = Modifier
                            .width(115.dp)
                            .testTag("country_code_box")
                    ) {
                        OutlinedTextField(
                            value = selectedCountryCode,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Code") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("country_code_input")
                        )

                        ExposedDropdownMenu(
                            expanded = countryDropdownExpanded,
                            onDismissRequest = { countryDropdownExpanded = false }
                        ) {
                            countryOptions.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text("${country.flag} ${country.code} (${country.name})") },
                                    onClick = {
                                        selectedCountryCode = country.code
                                        countryDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("country_option_${country.code}")
                                )
                            }
                        }
                    }

                    // 10-Digit Phone Input
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { newValue ->
                                val digitsOnly = newValue.filter { it.isDigit() }
                                if (digitsOnly.length <= 10) {
                                    contactPhone = digitsOnly
                                }
                            },
                            label = { Text("Phone (10 digits)") },
                            placeholder = { Text("5550192831") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(22.dp))
                            },
                            isError = phoneHasError,
                            textStyle = MaterialTheme.typography.titleMedium,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("caregiver_phone_input")
                        )
                    }
                }

                // Error feedback directly under phone field
                if (phoneHasError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ Phone number must be exactly 10 digits (${phoneDigits.length}/10 entered)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_validation_error_text")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Validation status feedback banner
                if (!isFormValid) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("caregiver_validation_banner")
                    ) {
                        Text(
                            text = when {
                                !isNameValid -> "⚠️ Caregiver Name is required."
                                !isRelationshipValid -> "⚠️ Relationship must be selected (Son, Daughter, or Other)."
                                !isPhoneValid -> "⚠️ Caregiver phone number must be exactly 10 digits."
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
                            text = "✅ Valid contact: $selectedCountryCode $contactPhone ($contactName). Tap Continue to proceed.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Bottom Navigation Action
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (isFormValid) {
                            FirebaseRepository.completeOnboarding()
                            onCompleteOnboarding()
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("caregiver_continue_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

