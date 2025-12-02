package com.code.EstesioTech

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.code.EstesioTech.ui.theme.EstesioTechTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EstesioTechTheme {
                RegisterScreen(
                    onRegisterClick = { name, license, pass, confirm ->
                        if (name.isEmpty() || license.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                        } else if (pass != confirm) {
                            Toast.makeText(this, "Senhas não conferem", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    },
                    onLoginClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CRIAR CONTA",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Campo Nome
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome completo") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo CRM
        OutlinedTextField(
            value = license,
            onValueChange = { license = it },
            label = { Text("CRM ou CDEnf") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo Senha
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo Confirmar Senha
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onRegisterClick(name, license, password, confirmPassword) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Cadastrar", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = {
                    name = ""; license = ""; password = ""; confirmPassword = ""
                },
                modifier = Modifier
                    .weight(0.5f)
                    .height(50.dp)
            ) {
                Text("Limpar", color = MaterialTheme.colorScheme.secondary, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Já tem uma conta? Faça o login",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
            modifier = Modifier
                .clickable { onLoginClick() }
                .padding(8.dp)
        )
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
    focusedLabelColor = MaterialTheme.colorScheme.secondary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)