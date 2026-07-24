package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.FirebaseRepository

enum class LoginStep {
    PHONE_INPUT,
    OTP_VERIFICATION,
    EMAIL_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val generatedOtpCode by FirebaseRepository.generatedOtp.collectAsStateWithLifecycle()

    var currentStep by remember { mutableStateOf(LoginStep.PHONE_INPUT) }
    var phoneNumber by remember { mutableStateOf("+1 (555) 234-5678") }
    var otpDigits by remember { mutableStateOf(List(6) { "" }) }
    var email by remember { mutableStateOf("sarah.jenkins@example.com") }
    var password by remember { mutableStateOf("••••••••") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timerSeconds by remember { mutableIntStateOf(30) }

    // Timer effect for OTP resend
    LaunchedEffect(currentStep, timerSeconds) {
        if (currentStep == LoginStep.OTP_VERIFICATION && timerSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            timerSeconds--
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentStep) {
                            LoginStep.PHONE_INPUT -> "Phone Login"
                            LoginStep.OTP_VERIFICATION -> "Verify OTP"
                            LoginStep.EMAIL_PASSWORD -> "Email Sign In"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentStep == LoginStep.OTP_VERIFICATION) {
                                currentStep = LoginStep.PHONE_INPUT
                            } else {
                                onNavigateBack()
                            }
                        },
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

            when (currentStep) {
                LoginStep.PHONE_INPUT -> {
                    Text(
                        text = "Sign In with Phone",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter your phone number to receive a 6-digit OTP code",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number", style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.titleMedium,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_number_input")
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            if (phoneNumber.isNotBlank()) {
                                isLoading = true
                                errorMessage = null
                                val generatedCode = FirebaseRepository.sendOtp(phoneNumber)
                                // Pre-fill digits for effortless testing or keep blank for typing
                                currentStep = LoginStep.OTP_VERIFICATION
                                timerSeconds = 30
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("send_otp_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Text("Send OTP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { currentStep = LoginStep.EMAIL_PASSWORD },
                        modifier = Modifier.testTag("switch_to_email_login")
                    ) {
                        Text(
                            text = "Use Email & Password instead",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
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

                LoginStep.OTP_VERIFICATION -> {
                    Text(
                        text = "Enter 6-Digit OTP",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Code sent to $phoneNumber",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // Display Generated OTP Banner for testing ease
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_code_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🔑 Test OTP Code: ${generatedOtpCode ?: "123456"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            TextButton(
                                onClick = {
                                    val code = generatedOtpCode ?: "123456"
                                    otpDigits = code.map { it.toString() }
                                },
                                modifier = Modifier.testTag("auto_fill_otp_button")
                            ) {
                                Text("Auto Fill", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 6-Digit OTP Box Grid
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 0 until 6) {
                            OutlinedTextField(
                                value = otpDigits.getOrElse(i) { "" },
                                onValueChange = { value ->
                                    if (value.length <= 1) {
                                        val newDigits = otpDigits.toMutableList()
                                        newDigits[i] = value
                                        otpDigits = newDigits
                                    }
                                },
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .testTag("otp_digit_$i")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Resend & Change Phone Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                currentStep = LoginStep.PHONE_INPUT
                                errorMessage = null
                            },
                            modifier = Modifier.testTag("change_phone_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change Number", style = MaterialTheme.typography.bodyMedium)
                        }

                        TextButton(
                            onClick = {
                                if (timerSeconds == 0) {
                                    FirebaseRepository.sendOtp(phoneNumber)
                                    timerSeconds = 30
                                    errorMessage = null
                                }
                            },
                            enabled = timerSeconds == 0,
                            modifier = Modifier.testTag("resend_otp_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (timerSeconds > 0) "Resend in ${timerSeconds}s" else "Resend OTP",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            val enteredCode = otpDigits.joinToString("")
                            if (enteredCode.length == 6) {
                                isLoading = true
                                val success = FirebaseRepository.verifyOtp(phoneNumber, enteredCode)
                                isLoading = false
                                if (success) {
                                    errorMessage = null
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Invalid OTP code. Please enter the correct 6-digit code."
                                }
                            } else {
                                errorMessage = "Please enter all 6 digits of the OTP."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("verify_otp_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Text("Verify OTP & Sign In", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                LoginStep.EMAIL_PASSWORD -> {
                    Text(
                        text = "Email & Password Sign In",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sign in using your account credentials",
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

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { currentStep = LoginStep.PHONE_INPUT },
                        modifier = Modifier.testTag("switch_to_phone_login")
                    ) {
                        Text(
                            text = "Use Phone Number & OTP instead",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


