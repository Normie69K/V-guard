package com.normie69K.v_guard.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.normie69K.v_guard.data.repository.AuthRepository
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    // Animate entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        delay(2_200L)
        if (authRepository.isUserLoggedIn()) onNavigateToDashboard()
        else onNavigateToLogin()
    }

    val scale by animateFloatAsState(
        targetValue    = if (visible) 1f else 0.7f,
        animationSpec  = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label          = "splashScale"
    )
    val alpha by animateFloatAsState(
        targetValue    = if (visible) 1f else 0f,
        animationSpec  = tween(600),
        label          = "splashAlpha"
    )

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .scale(scale)
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector     = Icons.Default.Security,
                contentDescription = null,
                modifier        = Modifier.size(96.dp),
                tint            = MaterialTheme.colorScheme.primary
            )
            Text(
                "V-Guard",
                style      = MaterialTheme.typography.displayLarge,
                color      = MaterialTheme.colorScheme.primary
            )
            Text(
                "Vehicle Safety System",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Loading indicator at bottom
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .alpha(alpha),
            color        = MaterialTheme.colorScheme.primary,
            trackColor   = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
