package com.normie69K.v_guard.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.normie69K.v_guard.data.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onBackToLogin: () -> Unit) {
    val authRepository = remember { AuthRepository() }

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun sendResetLink() {
        if (email.isBlank()) { errorMessage = "Please enter your email"; return }
        isLoading = true
        errorMessage = ""

        authRepository.sendPasswordResetEmail(
            email = email.trim(),
            onSuccess = {
                isLoading = false
                isSent = true
            },
            onFailure = { err ->
                isLoading = false
                errorMessage = err
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
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
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(24.dp))

            if (isSent) {
                Text(
                    "Check your email!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "We've sent a password reset link to\n$email",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onBackToLogin,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Return to Login", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    "Forgot Password?",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Enter the email associated with your account and we'll send you a link to reset your password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = "" },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = errorMessage.isNotEmpty()
                )

                AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    )
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = { sendResetLink() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send Reset Link", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}