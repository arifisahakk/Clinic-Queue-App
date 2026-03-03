// FILE: app/src/main/java/com/clinic/queue/TVScreen.kt
package com.clinic.queue

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*

// ── WAITING ROOM TV DISPLAY ──────────────────────────────────
@Composable
fun TVScreen() {
    var counterA     by remember { mutableStateOf<Int?>(null) }
    var counterB     by remember { mutableStateOf<Int?>(null) }
    var waitingCount by remember { mutableStateOf(0L) }

    val db = FirebaseDatabase.getInstance().getReference()

    // Listen to Counter A
    DisposableEffect(Unit) {
        val ref = db.child("counters").child("A").child("serving")
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { counterA = s.getValue(Int::class.java) }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    // Listen to Counter B
    DisposableEffect(Unit) {
        val ref = db.child("counters").child("B").child("serving")
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { counterB = s.getValue(Int::class.java) }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    // Listen to waiting count
    DisposableEffect(Unit) {
        val ref = db.child("queue").child("waiting")
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { waitingCount = s.childrenCount }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    // Blinking dot to show live
    val blink = rememberInfiniteTransition(label = "blink")
    val dotAlpha by blink.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "dot"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏥  CLINIC QUEUE", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AccentRed)
            Spacer(Modifier.width(12.dp))
            Text("●", fontSize = 14.sp, color = AccentGreen, modifier = Modifier.alpha(dotAlpha))
            Text(" LIVE", fontSize = 12.sp, color = AccentGreen)
        }

        Spacer(Modifier.height(32.dp))

        // Counter cards row
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CounterCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "COUNTER A",
                number = counterA,
                accentColor = AccentBlue
            )
            CounterCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "COUNTER B",
                number = counterB,
                accentColor = AccentGreen
            )
        }

        Spacer(Modifier.height(24.dp))

        // Footer
        Text(
            "Patients Waiting: $waitingCount",
            fontSize = 20.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CounterCard(
    modifier: Modifier = Modifier,
    label: String,
    number: Int?,
    accentColor: Color
) {
    Box(
        modifier = modifier.background(CardBg, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 22.sp, color = accentColor, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            AnimatedContent(
                targetState = number,
                transitionSpec = {
                    slideInVertically { it } togetherWith slideOutVertically { -it }
                },
                label = "counter_$label"
            ) { n ->
                Text(
                    text = n?.toString() ?: "---",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}