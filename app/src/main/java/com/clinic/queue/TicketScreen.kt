// FILE: app/src/main/java/com/clinic/queue/TicketScreen.kt
package com.clinic.queue

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

// ── QUEUE TICKET SCREEN ──────────────────────────────────────
@Composable
fun TicketScreen(nav: NavHostController, queueNumber: Int, patientName: String) {
    var countdown by remember { mutableStateOf(10) }

    // Pulsing animation on the number
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    // Auto-return after 10 seconds
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        nav.navigate("tablet") {
            popUpTo("tablet") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Hello,", fontSize = 20.sp, color = TextMuted)
        Text(
            patientName,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(32.dp))

        Text(
            "YOUR QUEUE NUMBER",
            fontSize = 14.sp,
            color = AccentRed,
            letterSpacing = 3.sp
        )
        Spacer(Modifier.height(20.dp))

        // Big number in a circle
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(scale)
                .background(CardBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                queueNumber.toString(),
                fontSize = 88.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(40.dp))
        Text(
            "Please have a seat.\nWe will call your number soon.",
            fontSize = 18.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        Text(
            "Returning in $countdown seconds...",
            fontSize = 13.sp,
            color = Color(0xFF444444)
        )

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = {
            nav.navigate("tablet") { popUpTo("tablet") { inclusive = true } }
        }) {
            Text("Done", color = AccentGreen)
        }
    }
}