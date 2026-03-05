package com.normie69K.v_guard.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.normie69K.v_guard.data.repository.FirebaseDbHelper

@Composable
fun LinkDeviceScreen(onDeviceLinked: () -> Unit) {
    val dbHelper = remember { FirebaseDbHelper() }

    var espId        by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    var isSuccess    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement    = Arrangement.Center,
        horizontalAlignment    = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector     = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Cable,
            contentDescription = null,
            modifier        = Modifier.size(80.dp),
            tint            = if (isSuccess) MaterialTheme.colorScheme.secondary
                              else MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text      = if (isSuccess) "Device Linked!" else "Link Your Vehicle",
            style     = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = if (isSuccess)
                "Your ESP32 has been successfully paired with your account."
            else
                "Enter the MAC Address from your ESP32 module to connect it to your account.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))

        AnimatedVisibility(!isSuccess) {
            Column {
                OutlinedTextField(
                    value         = espId,
                    onValueChange = { espId = it.uppercase(); errorMessage = "" },
                    label         = { Text("ESP32 Device ID  (e.g. AC:67:B2:…)") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(14.dp),
                    singleLine    = true,
                    isError       = errorMessage.isNotEmpty(),
                    supportingText = if (errorMessage.isNotEmpty()) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        when {
                            espId.isBlank() -> { errorMessage = "Device ID cannot be empty"; return@Button }
                        }
                        isLoading    = true
                        errorMessage = ""
                        dbHelper.linkDeviceToUser(
                            espId     = espId.trim(),
                            onSuccess = { isLoading = false; isSuccess = true },
                            onFailure = { err -> isLoading = false; errorMessage = err }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(14.dp),
                    enabled  = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color       = MaterialTheme.colorScheme.onPrimary,
                            modifier    = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Connect Device", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        AnimatedVisibility(isSuccess) {
            Button(
                onClick  = { onDeviceLinked() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Continue to Dashboard", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
