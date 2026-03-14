package com.normie69K.v_guard.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.normie69K.v_guard.data.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    onBackToLogin: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authRepository = remember { AuthRepository() }

    // States
    var phoneNumber by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var otpCode by remember { mutableStateOf("") }

    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Limits
    var attemptCount by remember { mutableStateOf(0) }
    val maxAttempts = 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone Verification") },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isOtpSent) "Enter Security Code" else "Secure Login",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isOtpSent) "We've sent a 6-digit OTP to $countryCode $phoneNumber"
                else "Enter your phone number to receive a verification code.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isOtpSent) {
                // ── PHONE NUMBER INPUT ──
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Country Code
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = { if (it.length <= 4) countryCode = it },
                        label = { Text("Code") },
                        modifier = Modifier.weight(0.3f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Phone Number
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 15) phoneNumber = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.weight(0.7f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (attemptCount >= maxAttempts) {
                    Text(
                        text = "You have reached the maximum number of attempts (3). Try again later.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (phoneNumber.isNotBlank() && activity != null) {
                            isLoading = true
                            attemptCount++
                            val fullNumber = "$countryCode$phoneNumber"

                            authRepository.sendOtp(
                                phoneNumber = fullNumber,
                                activity = activity,
                                onCodeSent = {
                                    isLoading = false
                                    isOtpSent = true
                                    Toast.makeText(context, "OTP Sent!", Toast.LENGTH_SHORT).show()
                                },
                                onVerificationFailed = { e ->
                                    isLoading = false
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                },
                                onVerificationCompleted = {
                                    // Auto-verification succeeded
                                    isLoading = false
                                    onAuthSuccess()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Enter a valid number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading && attemptCount < maxAttempts && phoneNumber.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send OTP (Attempt ${attemptCount}/$maxAttempts)")
                    }
                }

            } else {
                // ── OTP INPUT ──
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    label = { Text("6-Digit OTP") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (otpCode.length == 6) {
                            isLoading = true
                            authRepository.verifyOtp(
                                code = otpCode,
                                onSuccess = {
                                    isLoading = false
                                    Toast.makeText(context, "Verification Successful!", Toast.LENGTH_SHORT).show()
                                    onAuthSuccess()
                                },
                                onFailure = { e ->
                                    isLoading = false
                                    Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading && otpCode.length == 6
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Verify & Login")
                    }
                }

                TextButton(
                    onClick = { isOtpSent = false },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Change Phone Number")
                }
            }
        }
    }
}