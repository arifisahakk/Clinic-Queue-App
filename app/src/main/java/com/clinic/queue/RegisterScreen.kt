// FILE: app/src/main/java/com/clinic/queue/RegisterScreen.kt
package com.clinic.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.google.firebase.database.FirebaseDatabase

// ── REGISTRATION SCREEN ──────────────────────────────────────
@Composable
fun RegisterScreen(nav: NavHostController, prefilledIC: String) {
    var name     by remember { mutableStateOf("") }
    var ic       by remember { mutableStateOf(prefilledIC) }
    var address  by remember { mutableStateOf("") }
    var message  by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = ic,
            onValueChange = { ic = it },
            label = { Text("IC Number (e.g. 123456-78-9810)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Home Address") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(message, color = AccentRed, fontSize = 14.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isBlank() || ic.isBlank() || address.isBlank()) {
                    message = "Please fill in all fields"
                    return@Button
                }
                isLoading = true
                val icKey = ic.trim().replace("-", "")
                val patient = mapOf(
                    "name"     to name.trim(),
                    "idNumber" to ic.trim(),
                    "address"  to address.trim()
                )
                FirebaseDatabase.getInstance().getReference("patients")
                    .child(icKey).setValue(patient)
                    .addOnSuccessListener {
                        isLoading = false
                        // Go back to tablet so patient can check in
                        nav.navigate("tablet") {
                            popUpTo("tablet") { inclusive = true }
                        }
                    }
                    .addOnFailureListener {
                        isLoading = false
                        message = "Registration failed. Try again."
                    }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("REGISTER & CHECK IN", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { nav.popBackStack() }) {
            Text("← Back", color = TextMuted)
        }
    }
}