package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("sarah.jenkins@example.com") }
    var password by remember { mutableStateOf("••••••••") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Account Login",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("login_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Welcome to MedReminder",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sign in to securely synchronize your prescriptions & family alerts",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", style = MaterialTheme.typography.bodyLarge) },
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(28.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", style = MaterialTheme.typography.bodyLarge) },
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(28.dp)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    isLoading = true
                    onLoginSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("submit_login_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Text("Sign In", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("continue_as_guest_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Continue as Guest / Local Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

