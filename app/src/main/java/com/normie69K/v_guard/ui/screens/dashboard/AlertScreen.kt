package com.normie69K.v_guard.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AlertScreen(
    onCancelAlert: () -> Unit,
    onTriggerSos: () -> Unit
) {
    // 15-second countdown timer
    var timeLeft by remember { mutableIntStateOf(15) }
    var isCancelled by remember { mutableStateOf(false) }

    // This effect runs the countdown
    LaunchedEffect(key1 = timeLeft, key2 = isCancelled) {
        if (!isCancelled) {
            if (timeLeft > 0) {
                delay(1000L) // Wait 1 second
                timeLeft--
            } else {
                // Timer hit 0, trigger the emergency protocol automatically!
                onTriggerSos()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Accident Warning",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ACCIDENT DETECTED",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Emergency contacts will be notified in:",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Big Countdown Text
        Text(
            text = "$timeLeft s",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Cancel Button (False Alarm)
        Button(
            onClick = {
                isCancelled = true
                onCancelAlert() // This should update Firebase 'is_accident' back to false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("I'M SAFE (CANCEL)", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Immediate SOS Button
        Button(
            onClick = {
                isCancelled = true
                onTriggerSos()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("SEND SOS NOW", fontSize = 18.sp, color = MaterialTheme.colorScheme.onError)
        }
    }
}