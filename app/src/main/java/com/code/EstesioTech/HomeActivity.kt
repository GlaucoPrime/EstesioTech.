package com.code.EstesioTech

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.code.EstesioTech.ui.theme.EstesioTechTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EstesioTechTheme {
                val context = LocalContext.current
                var isConnected by remember { mutableStateOf(BleManager.isConnected()) }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            isConnected = BleManager.isConnected()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                HomeScreen(
                    isConnected = isConnected,
                    onConnectClick = {
                        if (isConnected) Toast.makeText(context, "Dispositivo já conectado.", Toast.LENGTH_SHORT).show()
                        else startActivity(Intent(context, MainActivity::class.java))
                    },
                    onStartTestClick = {
                        if (isConnected) startActivity(Intent(context, SelectionActivity::class.java))
                        else Toast.makeText(context, "Conecte o hardware primeiro!", Toast.LENGTH_LONG).show()
                    },
                    onHistoryClick = { Toast.makeText(context, "Histórico em Nuvem (Em breve)", Toast.LENGTH_SHORT).show() },
                    onLogoutClick = {
                        EstesioCloud.logout()
                        startActivity(Intent(context, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    onStartTestClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF101820), Color(0xFF000000))))
            .padding(24.dp)
    ) {
        // CABEÇALHO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Bem-vindo(a)", color = Color.Gray, fontSize = 14.sp)
                Text("Painel de Controle", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onLogoutClick) {
                Icon(Icons.Default.ExitToApp, "Sair", tint = Color(0xFFCF6679))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // STATUS DO SISTEMA
        StatusCard(isConnected)

        Spacer(modifier = Modifier.height(24.dp))
        Text("MENU RÁPIDO", color = Color(0xFF00ACC1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // GRID DE BOTÕES
        Row(modifier = Modifier.fillMaxWidth()) {
            DashboardButton(
                title = "Parear\nHardware",
                icon = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                color = if (isConnected) Color(0xFF4CAF50) else Color(0xFF2196F3),
                modifier = Modifier.weight(1f),
                onClick = onConnectClick
            )
            Spacer(modifier = Modifier.width(16.dp))
            DashboardButton(
                title = "Novo\nTeste",
                icon = Icons.Default.PlayArrow,
                color = if (isConnected) Color(0xFF00ACC1) else Color.Gray,
                modifier = Modifier.weight(1f),
                onClick = onStartTestClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DashboardButton(
            title = "Histórico do Paciente (Nuvem)",
            icon = Icons.Default.History,
            color = Color(0xFFFF9800),
            modifier = Modifier.fillMaxWidth(),
            onClick = onHistoryClick,
            isHorizontal = true
        )
    }
}

@Composable
fun StatusCard(isConnected: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2634).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if(isConnected) Color(0xFF4CAF50).copy(alpha=0.3f) else Color(0xFFF44336).copy(alpha=0.3f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Status da Conexão", color = Color.Gray, fontSize = 12.sp)
                Text(
                    text = if (isConnected) "Estesiômetro Online" else "Desconectado",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun DashboardButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isHorizontal: Boolean = false
) {
    Card(
        modifier = modifier
            .height(if (isHorizontal) 80.dp else 140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        if (isHorizontal) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(40.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}