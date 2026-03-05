package com.normie69K.v_guard.ui.screens.auth

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    onBackToLogin: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()

    var isOtpSent       by remember { mutableStateOf(false) }
    var phoneNumber     by remember { mutableStateOf("") }
    var otpCode         by remember { mutableStateOf("") }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }
    var verificationId  by remember { mutableStateOf("") }

    // ── Firebase phone auth callbacks ─────────────────────────────────────────
    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification (e.g. on emulators or same device)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { onAuthSuccess() }
                    .addOnFailureListener { e -> errorMessage = e.message ?: "Verification failed" }
            }
            override fun onVerificationFailed(e: FirebaseException) {
                isLoading    = false
                errorMessage = e.message ?: "Failed to send code. Check your number."
            }
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                isLoading      = false
                isOtpSent      = true
                errorMessage   = ""
            }
        }
    }

    fun sendCode() {
        if (phoneNumber.isBlank()) { errorMessage = "Enter a phone number"; return }
        isLoading    = true
        errorMessage = ""
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber.trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode() {
        if (otpCode.length != 6) { errorMessage = "Enter the full 6-digit code"; return }
        if (verificationId.isBlank()) { errorMessage = "Session expired. Go back and retry."; return }
        isLoading    = true
        errorMessage = ""
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { isLoading = false; onAuthSuccess() }
            .addOnFailureListener { e ->
                isLoading    = false
                errorMessage = e.message ?: "Incorrect code. Try again."
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isOtpSent) "Verify Code" else "Phone Login") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isOtpSent) { isOtpSent = false; otpCode = "" } else onBackToLogin()
                    }) {
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
            verticalArrangement    = Arrangement.Center,
            horizontalAlignment    = Alignment.CenterHorizontally
        ) {
            AnimatedContent(targetState = isOtpSent, transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            }) { otpSent ->
                if (!otpSent) {
                    // ── Step 1: phone number entry ────────────────────────────
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Phone, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(24.dp))
                        Text("Enter Phone Number", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "We'll send a 6-digit code to verify your number.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(36.dp))
                        OutlinedTextField(
                            value         = phoneNumber,
                            onValueChange = { phoneNumber = it; errorMessage = "" },
                            label         = { Text("Phone (e.g. +91 98765 43210)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(14.dp),
                            singleLine    = true,
                            isError       = errorMessage.isNotEmpty()
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                        }
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick  = { sendCode() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape    = RoundedCornerShape(14.dp),
                            enabled  = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            else Text("Send Verification Code", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // ── Step 2: OTP entry ─────────────────────────────────────
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Message, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(24.dp))
                        Text("Enter Verification Code", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Sent to $phoneNumber",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(36.dp))
                        OutlinedTextField(
                            value         = otpCode,
                            onValueChange = { if (it.length <= 6) { otpCode = it; errorMessage = "" } },
                            label         = { Text("6-Digit Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(14.dp),
                            singleLine    = true,
                            isError       = errorMessage.isNotEmpty()
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                        }
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick  = { verifyCode() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape    = RoundedCornerShape(14.dp),
                            enabled  = otpCode.length == 6 && !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            else Text("Verify & Sign In", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
