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
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToPhoneAuth: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }

    fun attemptLogin() {
        when {
            email.isBlank()    -> { errorMessage = "Email cannot be empty"; return }
            password.isBlank() -> { errorMessage = "Password cannot be empty"; return }
        }
        isLoading    = true
        errorMessage = ""
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { onLoginSuccess() }
            .addOnFailureListener { e ->
                isLoading    = false
                errorMessage = e.message ?: "Sign-in failed. Please try again."
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement    = Arrangement.Center,
        horizontalAlignment    = Alignment.CenterHorizontally
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Icon(
            imageVector     = Icons.Default.Security,
            contentDescription = "V-Guard Logo",
            modifier        = Modifier.size(80.dp),
            tint            = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "V-Guard",
            style      = MaterialTheme.typography.displayLarge,
            color      = MaterialTheme.colorScheme.primary
        )
        Text(
            "Secure Vehicle Monitoring",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(48.dp))

        // ── Email field ───────────────────────────────────────────────────────
        OutlinedTextField(
            value           = email,
            onValueChange   = { email = it; errorMessage = "" },
            label           = { Text("Email Address") },
            leadingIcon     = { Icon(Icons.Default.Email, null) },
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(14.dp),
            singleLine      = true,
            isError         = errorMessage.isNotEmpty()
        )

        Spacer(Modifier.height(12.dp))

        // ── Password field ────────────────────────────────────────────────────
        OutlinedTextField(
            value               = password,
            onValueChange       = { password = it; errorMessage = "" },
            label               = { Text("Password") },
            leadingIcon         = { Icon(Icons.Default.Lock, null) },
            trailingIcon        = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier            = Modifier.fillMaxWidth(),
            shape               = RoundedCornerShape(14.dp),
            singleLine          = true,
            isError             = errorMessage.isNotEmpty()
        )

        // ── Error message ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = errorMessage.isNotEmpty(),
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            Text(
                text     = errorMessage,
                color    = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── Sign In button ────────────────────────────────────────────────────
        Button(
            onClick  = { attemptLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape   = RoundedCornerShape(14.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color       = MaterialTheme.colorScheme.onPrimary,
                    modifier    = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign In", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(36.dp))

        // ── Divider ───────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "  or  ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        }

        Spacer(Modifier.height(28.dp))

        // ── Phone auth button ─────────────────────────────────────────────────
        OutlinedButton(
            onClick  = { onNavigateToPhoneAuth() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Phone, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Continue with Phone Number", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
