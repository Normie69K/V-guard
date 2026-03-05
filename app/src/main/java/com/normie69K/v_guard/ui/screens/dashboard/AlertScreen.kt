package com.normie69K.v_guard.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.normie69K.v_guard.data.repository.FirebaseDbHelper
import com.normie69K.v_guard.services.SmsManagerHelper
import kotlinx.coroutines.delay

private const val COUNTDOWN_SECONDS = 15

@Composable
fun AlertScreen(
    crashLat: Double,
    crashLng: Double,
    onCancelAlert: () -> Unit,
    onSosSent: () -> Unit
) {
    val context     = LocalContext.current
    val dbHelper    = remember { FirebaseDbHelper() }
    val smsHelper   = remember { SmsManagerHelper(context) }

    var timeLeft    by remember { mutableIntStateOf(COUNTDOWN_SECONDS) }
    var isCancelled by remember { mutableStateOf(false) }
    var sosFired    by remember { mutableStateOf(false) }

    // Load contacts once
    var contacts by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        dbHelper.getEmergencyContacts { contacts = it }
    }

    // Single reusable SOS action — guarded against double-fire
    fun fireSos() {
        if (sosFired || isCancelled) return
        sosFired = true
        smsHelper.sendEmergencySms(contacts, crashLat, crashLng)
        onSosSent()
    }

    // Countdown effect
    LaunchedEffect(timeLeft, isCancelled) {
        if (isCancelled) return@LaunchedEffect
        if (timeLeft > 0) {
            delay(1_000L)
            timeLeft--
        } else {
            fireSos()
        }
    }

    // Pulsing animation for the warning icon
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "iconScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Warning icon ─────────────────────────────────────────────────
            Icon(
                imageVector     = Icons.Default.Warning,
                contentDescription = "Accident Warning",
                tint            = MaterialTheme.colorScheme.error,
                modifier        = Modifier
                    .size(96.dp)
                    .scale(scale)
            )

            Text(
                "ACCIDENT DETECTED",
                fontSize    = 30.sp,
                fontWeight  = FontWeight.ExtraBold,
                color       = MaterialTheme.colorScheme.error,
                textAlign   = TextAlign.Center
            )

            Text(
                "Emergency contacts will be alerted in:",
                fontSize    = 16.sp,
                color       = MaterialTheme.colorScheme.onErrorContainer,
                textAlign   = TextAlign.Center
            )

            // ── Countdown ring ───────────────────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress    = { timeLeft / COUNTDOWN_SECONDS.toFloat() },
                    modifier    = Modifier.size(120.dp),
                    color       = MaterialTheme.colorScheme.error,
                    strokeWidth = 8.dp,
                    trackColor  = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
                Text(
                    "$timeLeft",
                    fontSize   = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.error
                )
            }

            if (contacts.isNotEmpty()) {
                Text(
                    "${contacts.size} contact${if (contacts.size > 1) "s" else ""} will be notified",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── I'm Safe ─────────────────────────────────────────────────────
            Button(
                onClick = {
                    isCancelled = true
                    onCancelAlert()
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B5E20),
                    contentColor   = Color.White
                )
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text("I'M SAFE  —  CANCEL", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            // ── Send SOS Now ──────────────────────────────────────────────────
            Button(
                onClick  = { fireSos() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError
                ),
                enabled  = !sosFired
            ) {
                if (sosFired) {
                    CircularProgressIndicator(
                        color    = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("SEND SOS NOW", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
