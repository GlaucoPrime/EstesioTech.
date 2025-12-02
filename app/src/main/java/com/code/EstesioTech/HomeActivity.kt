package com.code.EstesioTech

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.code.EstesioTech.ui.theme.EstesioTechTheme
import com.code.EstesioTech.MainActivity

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EstesioTechTheme {
                val context = LocalContext.current
                var isConnected by remember { mutableStateOf(BleManager.isConnected()) }

                // Observar o ciclo de vida para atualizar o estado do botão ao voltar para a tela
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            isConnected = BleManager.isConnected()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                HomeScreen(
                    isConnected = isConnected,
                    onConnectClick = {
                        if (isConnected) {
                            Toast.makeText(context, "Você já está conectado.", Toast.LENGTH_SHORT).show()
                        } else {
                            startActivity(Intent(context, MainActivity::class.java))
                        }
                    },
                    onStartTestClick = {
                        if (isConnected) {
                            startActivity(Intent(context, SelectionActivity::class.java))
                        } else {
                            Toast.makeText(context, "Nenhum dispositivo conectado.", Toast.LENGTH_LONG).show()
                        }
                    },
                    onHistoryClick = {
                        Toast.makeText(context, "Histórico não implementado.", Toast.LENGTH_SHORT).show()
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
    onHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MENU PRINCIPAL",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        MenuButton(
            text = "CONECTAR AO DISPOSITIVO",
            onClick = onConnectClick,
            enabled = !isConnected
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "INICIAR TESTE",
            onClick = onStartTestClick,
            enabled = isConnected
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "HISTÓRICO DE TESTES",
            onClick = onHistoryClick,
            enabled = true
        )
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = Color.Gray
        )
    ) {
        Text(text = text, fontSize = 18.sp, color = Color.White)
    }
}