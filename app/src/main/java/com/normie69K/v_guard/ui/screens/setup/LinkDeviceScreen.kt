package com.normie69K.v_guard.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.normie69K.v_guard.data.repository.FirebaseDbHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkDeviceScreen(onDeviceLinked: () -> Unit) {
    val dbHelper = remember { FirebaseDbHelper() }
    val scrollState = rememberScrollState()

    var newEspId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val linkedDevices = remember { mutableStateListOf<String>() }

    // Fetch existing linked devices when the screen loads
    LaunchedEffect(Unit) {
        dbHelper.getLinkedDevices { devices ->
            linkedDevices.clear()
            linkedDevices.addAll(devices)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // ── Header with Dynamic Counter ───────────────────────────────────
            Icon(Icons.Default.Cable, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Manage Vehicles", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                // ── The Counter Badge ──
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (linkedDevices.size >= 5) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${linkedDevices.size}/5",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (linkedDevices.size >= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Link your ESP32 modules using their MAC Address.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // ── Input Row to Add New Device ───────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = newEspId,
                    onValueChange = { newEspId = it.uppercase(); errorMessage = "" },
                    label = { Text("ESP32 MAC (e.g. AC:67:B2...)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = linkedDevices.size < 5 && !isLoading
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        val idToLink = newEspId.trim()
                        when {
                            idToLink.isBlank() -> errorMessage = "Enter a MAC address"
                            linkedDevices.contains(idToLink) -> errorMessage = "Device already linked"
                            linkedDevices.size >= 5 -> errorMessage = "Max 5 vehicles allowed"
                            else -> {
                                isLoading = true
                                errorMessage = ""
                                dbHelper.linkDeviceToUser(
                                    espId = idToLink,
                                    onSuccess = {
                                        linkedDevices.add(idToLink)
                                        newEspId = ""
                                        isLoading = false
                                    },
                                    onFailure = { err ->
                                        errorMessage = err
                                        isLoading = false
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    enabled = linkedDevices.size < 5 && newEspId.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Add, "Add Device")
                    }
                }
            }

            AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── List of Linked Devices ────────────────────────────────────────
            if (linkedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No vehicles linked yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                linkedDevices.forEach { deviceId ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeveloperBoard, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Text(deviceId, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            }
                            IconButton(
                                onClick = {
                                    isLoading = true
                                    dbHelper.removeLinkedDevice(
                                        espId = deviceId,
                                        onSuccess = {
                                            linkedDevices.remove(deviceId)
                                            errorMessage = ""
                                            isLoading = false
                                        },
                                        onFailure = { err ->
                                            errorMessage = err
                                            isLoading = false
                                        }
                                    )
                                },
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // ── Continue Button ───────────────────────────────────────────────────
        Button(
            onClick = { onDeviceLinked() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(if (linkedDevices.isNotEmpty()) "Return to Dashboard" else "Skip for now", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}