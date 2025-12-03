package com.code.EstesioTech

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.code.EstesioTech.ui.theme.EstesioTechTheme

class SelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceAddress = intent.getStringExtra("DEVICE_ADDRESS")

        setContent {
            EstesioTechTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF101820)) {
                    SelectionScreen(
                        onBackClick = { finish() },
                        onOptionSelected = { bodyPart ->
                            try {
                                val intent = Intent(this, TesteActivity::class.java)
                                intent.putExtra("DEVICE_ADDRESS", deviceAddress)
                                intent.putExtra("BODY_PART", bodyPart)
                                startActivity(intent)
                            } catch (e: Exception) {
                                // Se der erro ao abrir, mostra na tela
                                Log.e("SelectionActivity", "Erro ao abrir tela", e)
                                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionScreen(onBackClick: () -> Unit, onOptionSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Selecione a área", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

        ExpandableSection("MÃOS", listOf("Mão Esquerda" to "mao_esquerda", "Mão Direita" to "mao_direita"), onOptionSelected)
        Spacer(modifier = Modifier.height(16.dp))
        ExpandableSection("PÉS", listOf("Pé Esquerdo" to "pe_esquerdo", "Pé Direito" to "pe_direito"), onOptionSelected)

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679)), modifier = Modifier.fillMaxWidth()) {
            Text("Voltar", color = Color.White)
        }
    }
}

@Composable
fun ExpandableSection(title: String, options: List<Pair<String, String>>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF2C3E50)).clickable { expanded = !expanded }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Icon(if (expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight, null, tint = Color.White)
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF34495E)).padding(8.dp)) {
                options.forEach { (label, code) ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { onOptionSelected(code) }.padding(12.dp)) {
                        Text(label, color = Color(0xFFBDC3C7), fontSize = 16.sp)
                    }
                }
            }
        }
    }
}