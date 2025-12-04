package com.code.EstesioTech

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.code.EstesioTech.ui.theme.EstesioTechTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (EstesioCloud.isUserLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        setContent {
            EstesioTechTheme {
                LoginScreen(
                    onLoginClick = { crm, uf, password ->
                        if (crm.isNotEmpty() && uf.isNotEmpty() && password.isNotEmpty()) {
                            Toast.makeText(this, "Autenticando...", Toast.LENGTH_SHORT).show()

                            // Login agora exige UF
                            EstesioCloud.login(crm, uf, password,
                                onSuccess = {
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                },
                                onError = { msg ->
                                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(this, "Preencha CRM, Estado e Senha.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginClick: (String, String, String) -> Unit, onRegisterClick: () -> Unit) {
    var crm by remember { mutableStateOf("") }
    var uf by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2634).copy(alpha = 0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ESTESIOTECH", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00ACC1), letterSpacing = 2.sp)
                Text("Acesso Profissional", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp))

                TechTextField(
                    value = crm,
                    onValueChange = { crm = it },
                    label = "CRM",
                    icon = Icons.Default.Badge,
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de Estado
                TechStateDropdown(selectedState = uf, onStateSelected = { uf = it })

                Spacer(modifier = Modifier.height(16.dp))

                TechTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Senha",
                    icon = Icons.Default.Lock,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onLoginClick(crm, uf, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, Color(0xFF00ACC1).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00ACC1).copy(alpha = 0.2f),
                        contentColor = Color(0xFF00ACC1)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ACESSAR SISTEMA", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Novo por aqui? Criar conta",
                    color = Color(0xFF80DEEA),
                    modifier = Modifier.clickable { onRegisterClick() },
                    fontSize = 14.sp
                )
            }
        }
    }
}