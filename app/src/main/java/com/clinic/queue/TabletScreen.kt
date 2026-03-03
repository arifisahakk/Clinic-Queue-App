
// FILE: app/src/main/java/com/clinic/queue/TabletScreen.kt
package com.clinic.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

// ── PATIENT TABLET SCREEN ────────────────────────────────────
@Composable
fun TabletScreen(nav: NavHostController) {
    var icInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏥", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Welcome to Our Clinic",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Enter your IC number to get your queue number",
            fontSize = 15.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = icInput,
            onValueChange = { icInput = it; message = "" },
            label = { Text("IC Number (e.g. 123456-78-9810)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(message, color = AccentRed, fontSize = 14.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (icInput.isBlank()) {
                    message = "Please enter your IC number"
                    return@Button
                }
                isLoading = true
                checkPatient(
                    ic = icInput.trim(),
                    onFound = { name, icRaw ->
                        assignQueueNumber(
                            ic = icRaw,
                            name = name,
                            onSuccess = { number ->
                                isLoading = false
                                nav.navigate("ticket/$number/$name")
                                icInput = ""
                            },
                            onError = {
                                isLoading = false
                                message = "Failed to assign queue. Try again."
                            }
                        )
                    },
                    onNotFound = {
                        isLoading = false
                        message = "Your ID does not exist. Please register first."
                        nav.navigate("register/${icInput.trim()}")
                        icInput = ""
                    },
                    onError = {
                        isLoading = false
                        message = "Connection error. Please try again."
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("GET QUEUE NUMBER", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { nav.popBackStack() }) {
            Text("← Back to Menu", color = TextMuted)
        }
    }
}

// ── Firebase helpers ─────────────────────────────────────────

fun checkPatient(
    ic: String,
    onFound: (name: String, ic: String) -> Unit,
    onNotFound: () -> Unit,
    onError: () -> Unit
) {
    val icKey = ic.replace("-", "")
    FirebaseDatabase.getInstance().getReference("patients").child(icKey)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "Patient"
                    onFound(name, ic)
                } else {
                    onNotFound()
                }
            }
            override fun onCancelled(error: DatabaseError) = onError()
        })
}

fun assignQueueNumber(
    ic: String,
    name: String,
    onSuccess: (Int) -> Unit,
    onError: () -> Unit
) {
    val db = FirebaseDatabase.getInstance().getReference()
    db.child("queue").child("nextNumber").runTransaction(object : Transaction.Handler {
        override fun doTransaction(currentData: MutableData): Transaction.Result {
            val current = currentData.getValue(Int::class.java) ?: 1000
            currentData.value = current + 1
            return Transaction.success(currentData)
        }
        override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
            if (committed && snapshot != null) {
                val number = snapshot.getValue(Int::class.java) ?: run { onError(); return }
                db.child("queue").child("waiting").child(number.toString())
                    .setValue(QueueEntry(number, ic, name))
                onSuccess(number)
            } else {
                onError()
            }
        }
    })
}