package com.normie69K.v_guard.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.database.FirebaseDatabase

@Composable
fun LinkDeviceScreen(onDeviceLinked: () -> Unit) {
    var espId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = "Device Icon",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Link Your Vehicle", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter the unique MAC Address of your ESP32 module to connect it to your account.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = espId,
            onValueChange = { espId = it },
            label = { Text("ESP32 Device ID (e.g., AC:67:B2...)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (espId.isNotBlank()) {
                    isLoading = true
                    // TODO: Call the Firebase linking function here
                    // linkDeviceToFirebase(espId, onSuccess = {
                    //     isLoading = false
                    //     onDeviceLinked()
                    // }, onFailure = { error ->
                    //     isLoading = false
                    //     errorMessage = error
                    // })

                    // For now, simulating success:
                    onDeviceLinked()
                } else {
                    errorMessage = "Device ID cannot be empty"
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Connect Device")
            }
        }
    }
}