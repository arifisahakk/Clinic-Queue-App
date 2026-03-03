// FILE: app/src/main/java/com/clinic/queue/TabletScreen.kt
package com.clinic.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
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

// ── IC Number Visual Transformation ─────────────────────────
// Auto-formats raw digits into 123456-78-9010 as user types
class ICNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            digits.forEachIndexed { index, c ->
                when (index) {
                    6, 8 -> append('-')
                    else -> {}
                }
                append(c)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 6  -> offset
                    offset <= 8  -> offset + 1
                    offset <= 12 -> offset + 2
                    else         -> formatted.length
                }
            }
            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 6  -> offset
                    offset <= 9  -> offset - 1
                    offset <= 14 -> offset - 2
                    else         -> digits.length
                }
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

// ── Validators ───────────────────────────────────────────────
fun validateIC(digits: String): String? {
    if (digits.isEmpty()) return "IC number cannot be empty"
    if (digits.length < 12) return "IC number must be 12 digits  e.g. 123456-78-9010"
    return null
}

fun buildFormattedIC(digits: String): String {
    return buildString {
        digits.forEachIndexed { i, c ->
            when (i) { 6, 8 -> append('-') }
            append(c)
        }
    }
}

// ── PATIENT TABLET SCREEN ────────────────────────────────────
@Composable
fun TabletScreen(nav: NavHostController) {
    var icInput          by remember { mutableStateOf("") }
    var icError          by remember { mutableStateOf<String?>(null) }
    var message          by remember { mutableStateOf("") }
    var isLoading        by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Checking connection...") }

    // Firebase connection indicator
    DisposableEffect(Unit) {
        val ref = FirebaseDatabase.getInstance().getReference(".info/connected")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ok = snapshot.getValue(Boolean::class.java) ?: false
                connectionStatus = if (ok) "🟢 Firebase Connected" else "🔴 Not Connected"
            }
            override fun onCancelled(error: DatabaseError) {
                connectionStatus = "🔴 Connection Error"
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

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
        Spacer(Modifier.height(4.dp))
        Text(
            connectionStatus,
            fontSize = 13.sp,
            color = if (connectionStatus.contains("Connected")) AccentGreen else AccentRed
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Enter your IC number to get your queue number",
            fontSize = 15.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))

        // IC Number field
        // - Keyboard is numeric (no letters possible)
        // - Dashes inserted automatically by ICNumberTransformation
        // - Max 12 digits enforced
        OutlinedTextField(
            value = icInput,
            onValueChange = { raw ->
                // Strip non-digits and cap at 12
                val digits = raw.filter { it.isDigit() }.take(12)
                icInput = digits
                icError = null
                message = ""
            },
            label = { Text("IC Number") },
            placeholder = { Text("123456-78-9010", color = Color(0xFFAAAAAA)) },
            supportingText = {
                if (icError != null) {
                    Text(icError!!, color = AccentRed, fontSize = 12.sp)
                } else {
                    Text(
                        "Format: 123456-78-9010  (${icInput.length}/12 digits)",
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                }
            },
            isError = icError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            // Numeric keyboard — prevents letters from being typed
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            // Shows dashes automatically without storing them
            visualTransformation = ICNumberTransformation()
        )

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(message, color = AccentRed, fontSize = 14.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                // Validate before hitting Firebase
                val error = validateIC(icInput)
                if (error != null) {
                    icError = error
                    return@Button
                }

                val formattedIC = buildFormattedIC(icInput)
                isLoading = true

                checkPatient(
                    ic = formattedIC,
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
                        nav.navigate("register/$formattedIC")
                        icInput = ""
                    },
                    onError = {
                        isLoading = false
                        message = "Connection error. Please try again."
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("GET QUEUE NUMBER", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
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