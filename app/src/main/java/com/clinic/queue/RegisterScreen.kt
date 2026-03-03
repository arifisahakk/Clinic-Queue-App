// FILE: app/src/main/java/com/clinic/queue/RegisterScreen.kt
package com.clinic.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.FirebaseDatabase

// ── Name Validator ───────────────────────────────────────────
fun validateName(name: String): String? {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "Name cannot be empty"
    if (trimmed.length < 2) return "Name is too short"
    // No digits allowed in name
    if (trimmed.any { it.isDigit() }) return "Name must not contain numbers"
    // Only allow letters, spaces, dots, apostrophes, slashes (for A/L, A/P)
    val nameRegex = Regex("^[a-zA-Z\\s'./@-]+$")
    if (!nameRegex.matches(trimmed)) return "Name contains invalid characters"
    return null
}

// ── Address Validator ────────────────────────────────────────
fun validateAddress(address: String): String? {
    if (address.trim().isEmpty()) return "Address cannot be empty"
    if (address.trim().length < 5) return "Please enter a complete address"
    return null
}

// ── REGISTRATION SCREEN ──────────────────────────────────────
@Composable
fun RegisterScreen(nav: NavHostController, prefilledIC: String) {
    var name        by remember { mutableStateOf("") }
    var icInput     by remember { mutableStateOf(
        // Pre-fill with digits only from the IC passed in
        prefilledIC.filter { it.isDigit() }.take(12)
    )}
    var address     by remember { mutableStateOf("") }
    var nameError   by remember { mutableStateOf<String?>(null) }
    var icError     by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var isLoading   by remember { mutableStateOf(false) }
    var message     by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Text("📋", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))

        Text(
            "Patient Registration",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Your ID was not found.\nPlease register to continue.",
            fontSize = 14.sp,
            color = AccentRed,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        // ── Full Name Field ──────────────────────────────────
        // Letters only — digits blocked via onValueChange filter
        OutlinedTextField(
            value = name,
            onValueChange = { input ->
                // Block digits from being typed at all
                val filtered = input.filter { !it.isDigit() }
                name = filtered
                nameError = null
            },
            label = { Text("Full Name") },
            placeholder = { Text("e.g. Ahmad Bin Ali", color = Color(0xFFAAAAAA)) },
            supportingText = {
                if (nameError != null) {
                    Text(nameError!!, color = AccentRed, fontSize = 12.sp)
                } else {
                    Text("Letters only, no numbers allowed", color = Color(0xFF888888), fontSize = 12.sp)
                }
            },
            isError = nameError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            )
        )
        Spacer(Modifier.height(16.dp))

        // ── IC Number Field ──────────────────────────────────
        // Same auto-format as tablet screen
        OutlinedTextField(
            value = icInput,
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(12)
                icInput = digits
                icError = null
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ICNumberTransformation()
        )
        Spacer(Modifier.height(16.dp))

        // ── Address Field ────────────────────────────────────
        OutlinedTextField(
            value = address,
            onValueChange = {
                address = it
                addressError = null
            },
            label = { Text("Home Address") },
            placeholder = { Text("e.g. No 12, Jalan Merdeka, Melaka", color = Color(0xFFAAAAAA)) },
            supportingText = {
                if (addressError != null) {
                    Text(addressError!!, color = AccentRed, fontSize = 12.sp)
                }
            },
            isError = addressError != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            )
        )

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(message, color = AccentRed, fontSize = 14.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                // Validate all fields before saving
                val nErr = validateName(name)
                val iErr = validateIC(icInput)
                val aErr = validateAddress(address)

                nameError    = nErr
                icError      = iErr
                addressError = aErr

                // Stop if any field has an error
                if (nErr != null || iErr != null || aErr != null) return@Button

                isLoading = true
                val formattedIC = buildFormattedIC(icInput)
                val icKey = formattedIC.replace("-", "")

                val patient = mapOf(
                    "name"     to name.trim(),
                    "idNumber" to formattedIC,
                    "address"  to address.trim()
                )

                FirebaseDatabase.getInstance().getReference("patients")
                    .child(icKey).setValue(patient)
                    .addOnSuccessListener {
                        isLoading = false
                        nav.navigate("tablet") {
                            popUpTo("tablet") { inclusive = true }
                        }
                    }
                    .addOnFailureListener {
                        isLoading = false
                        message = "Registration failed. Try again."
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    "REGISTER & CHECK IN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { nav.popBackStack() }) {
            Text("← Back", color = TextMuted)
        }

        Spacer(Modifier.height(32.dp))
    }
}