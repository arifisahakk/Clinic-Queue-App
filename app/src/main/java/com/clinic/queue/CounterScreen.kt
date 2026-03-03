// FILE: app/src/main/java/com/clinic/queue/CounterScreen.kt
package com.clinic.queue

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.*

// ── COUNTER CLERK SCREEN ─────────────────────────────────────
@Composable
fun CounterScreen(nav: NavHostController, counterId: String) {
    var serving     by remember { mutableStateOf<Int?>(null) }
    var waitingCount by remember { mutableStateOf(0) }
    var isLoading   by remember { mutableStateOf(false) }
    var snackMsg    by remember { mutableStateOf("") }

    val db = FirebaseDatabase.getInstance().getReference()

    // Listen to this counter's current serving number
    DisposableEffect(counterId) {
        val ref = db.child("counters").child(counterId).child("serving")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serving = snapshot.getValue(Int::class.java)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    // Listen to waiting queue count
    DisposableEffect(Unit) {
        val ref = db.child("queue").child("waiting")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                waitingCount = snapshot.childrenCount.toInt()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    val accentColor = if (counterId == "A") AccentBlue else AccentGreen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "COUNTER $counterId",
            fontSize = 14.sp,
            color = accentColor,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text("Now Serving", fontSize = 18.sp, color = Color(0xFF888888))
        Spacer(Modifier.height(24.dp))

        // Animated number display
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = serving,
                transitionSpec = {
                    slideInVertically { it } togetherWith slideOutVertically { -it }
                },
                label = "serving_number"
            ) { number ->
                Text(
                    text = number?.toString() ?: "--",
                    fontSize = if (number != null) 80.sp else 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Patients waiting: $waitingCount",
            fontSize = 14.sp,
            color = Color(0xFF888888)
        )

        Spacer(Modifier.height(48.dp))

        // NEXT button
        Button(
            onClick = {
                isLoading = true
                callNextPatient(
                    counterId = counterId,
                    onSuccess = { isLoading = false },
                    onEmpty   = { isLoading = false; snackMsg = "No patients in queue" },
                    onError   = { isLoading = false; snackMsg = "Error. Try again." }
                )
            },
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
            } else {
                Text("▶  NEXT PATIENT", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (snackMsg.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(snackMsg, color = AccentRed, fontSize = 14.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { nav.popBackStack() }) {
            Text("← Back to Menu", color = TextMuted)
        }
    }
}

// ── Firebase: call next patient ──────────────────────────────
fun callNextPatient(
    counterId: String,
    onSuccess: () -> Unit,
    onEmpty:   () -> Unit,
    onError:   () -> Unit
) {
    val db = FirebaseDatabase.getInstance().getReference()
    db.child("queue").child("waiting")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    onEmpty(); return
                }
                // Pick the lowest queue number
                val nextKey = snapshot.children
                    .mapNotNull { it.key?.toIntOrNull() }
                    .minOrNull()
                    ?.toString() ?: run { onError(); return }

                db.child("queue").child("waiting").child(nextKey).removeValue()
                db.child("counters").child(counterId).child("serving")
                    .setValue(nextKey.toInt())
                onSuccess()
            }
            override fun onCancelled(error: DatabaseError) = onError()
        })
}