// FILE: app/src/main/java/com/clinic/queue/MainActivity.kt
package com.clinic.queue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// ── Colour palette ──────────────────────────────────────────
val DarkBg      = Color(0xFF0D1117)
val CardBg      = Color(0xFF161B22)
val AccentRed   = Color(0xFFE94560)
val AccentBlue  = Color(0xFF4FC3F7)
val AccentGreen = Color(0xFF56D364)
val TextPrimary = Color(0xFFE6EDF3)
val TextMuted   = Color(0xFF8B949E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "home") {
                composable("home")           { HomeScreen(navController) }
                composable("tablet")         { TabletScreen(navController) }
                composable("register/{ic}")  { back ->
                    RegisterScreen(navController, back.arguments?.getString("ic") ?: "")
                }
                composable("ticket/{number}/{name}") { back ->
                    TicketScreen(
                        navController,
                        back.arguments?.getString("number")?.toIntOrNull() ?: 0,
                        back.arguments?.getString("name") ?: ""
                    )
                }
                composable("counter/{id}") { back ->
                    CounterScreen(navController, back.arguments?.getString("id") ?: "A")
                }
                composable("tv") { TVScreen() }
            }
        }
    }
}

// ── HOME / MODE SELECTOR ─────────────────────────────────────
@Composable
fun HomeScreen(nav: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏥", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "CLINIC QUEUE",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = AccentRed
        )
        Text("Select device mode", fontSize = 15.sp, color = TextMuted)
        Spacer(Modifier.height(48.dp))

        ModeButton("📱  Patient Tablet",     Color(0xFF1F2D3D)) { nav.navigate("tablet") }
        Spacer(Modifier.height(16.dp))
        ModeButton("🖥️  Counter A — Clerk",  Color(0xFF1A3A2A)) { nav.navigate("counter/A") }
        Spacer(Modifier.height(16.dp))
        ModeButton("🖥️  Counter B — Clerk",  Color(0xFF1A3A2A)) { nav.navigate("counter/B") }
        Spacer(Modifier.height(16.dp))
        ModeButton("📺  Waiting Room TV",    Color(0xFF2A1F3D)) { nav.navigate("tv") }
    }
}

@Composable
fun ModeButton(label: String, bg: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg)
    ) {
        Text(label, fontSize = 17.sp, color = TextPrimary)
    }
}