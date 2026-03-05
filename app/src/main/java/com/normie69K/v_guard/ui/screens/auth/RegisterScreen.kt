package com.normie69K.v_guard.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.normie69K.v_guard.data.repository.AuthRepository

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun attemptRegister() {
        when {
            name.isBlank() -> { errorMessage = "Name cannot be empty"; return }
            email.isBlank() -> { errorMessage = "Email cannot be empty"; return }
            password.length < 6 -> { errorMessage = "Password must be at least 6 characters"; return }
        }
        isLoading = true
        errorMessage = ""

        authRepository.register(
            name = name.trim(),
            email = email.trim(),
            password = password,
            onSuccess = { onRegisterSuccess() },
            onFailure = { e ->
                isLoading = false
                errorMessage = e
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = "Sign Up",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text("Create Account", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(36.dp))

        // ── Name field ────────────────────────────────────────────────────────
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; errorMessage = "" },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // ── Email field ───────────────────────────────────────────────────────
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = "" },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // ── Password field ────────────────────────────────────────────────────
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = "" },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // ── Error message ─────────────────────────────────────────────────────
        AnimatedVisibility(visible = errorMessage.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, start = 4.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── Sign Up button ────────────────────────────────────────────────────
        Button(
            onClick = { attemptRegister() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Sign Up", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Back to Login ─────────────────────────────────────────────────────
        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log In", color = MaterialTheme.colorScheme.primary)
        }
    }
}