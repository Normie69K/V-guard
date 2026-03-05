package com.normie69K.v_guard.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.normie69K.v_guard.data.repository.AuthRepository

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToPhoneAuth: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // REQUIRED FOR GOOGLE LOGIN:
    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val auth = FirebaseAuth.getInstance()

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }

    // Fixed the double ".apps.googleusercontent.com"
    val webClientId = "132500161718-7hqjuonnarjnlsomg58b7efebpl7egim.apps.googleusercontent.com"

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    isLoading = true
                    authRepository.signInWithGoogle(
                        idToken = idToken,
                        onSuccess = { onLoginSuccess() },
                        onFailure = { err ->
                            isLoading = false
                            errorMessage = err
                        }
                    )
                } else {
                    errorMessage = "Google Sign-In failed: No ID Token"
                }
            } catch (e: ApiException) {
                isLoading = false
                Log.e("GoogleSignIn", "Google sign in failed", e)
                errorMessage = "Google sign in failed: ${e.message}"
            }
        } else {
            isLoading = false
        }
    }

    fun launchGoogleSignIn() {
        isLoading = true
        errorMessage = ""
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

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

        Spacer(Modifier.height(12.dp))

        // ── Google Auth Button (MISSING IN YOUR SNIPPET) ──────────────────────
        OutlinedButton(
            onClick  = { launchGoogleSignIn() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(14.dp),
            enabled  = !isLoading
        ) {
            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(16.dp))

        // ── Register Link ─────────────────────────────────────────────────────
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Sign Up", color = MaterialTheme.colorScheme.primary)
        }
    }
}