package com.code.EstesioTech

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.code.EstesioTech.ui.theme.EstesioTechTheme // Certifique-se que este import está correto

// Cores personalizadas para combinar com seu design anterior
val DarkBackground = Color(0xFF101820)
val HeaderColor = Color(0xFF2C3E50)
val OptionColor = Color(0xFF34495E)
val TextColor = Color(0xFFFFFFFF)
val TextHintColor = Color(0xFFBDC3C7)

class SelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recupera o endereço que veio da tela anterior (se houver)
        val deviceAddress = intent.getStringExtra("DEVICE_ADDRESS")

        setContent {
            EstesioTechTheme {
                // Surface configura a cor de fundo da tela inteira
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    SelectionScreen(
                        onBackClick = { finish() },
                        onOptionSelected = { bodyPart ->
                            openTestScreen(bodyPart, deviceAddress)
                        }
                    )
                }
            }
        }
    }

    private fun openTestScreen(bodyPart: String, address: String?) {
        val intent = Intent(this, TesteActivity::class.java)
        // Repassa o endereço do bluetooth se existir
        if (address != null) {
            intent.putExtra("DEVICE_ADDRESS", address)
        }
        // Passa qual parte do corpo foi selecionada (opcional, mas útil)
        intent.putExtra("BODY_PART", bodyPart)

        startActivity(intent)
    }
}

@Composable
fun SelectionScreen(
    onBackClick: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    // Permite rolar a tela se for pequena
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "Selecione a área",
            color = TextColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Seção MÃOS
        ExpandableSection(
            title = "MÃOS",
            options = listOf(
                "Mão Esquerda" to "mao_esquerda",
                "Mão Direita" to "mao_direita"
            ),
            onOptionSelected = onOptionSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Seção PÉS
        ExpandableSection(
            title = "PÉS",
            options = listOf(
                "Pé Esquerdo" to "pe_esquerdo",
                "Pé Direito" to "pe_direito"
            ),
            onOptionSelected = onOptionSelected
        )

        Spacer(modifier = Modifier.weight(1f)) // Empurra o botão voltar para baixo

        // Botão Voltar
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Voltar", color = Color.White)
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    options: List<Pair<String, String>>, // Par: Texto visível -> Código interno
    onOptionSelected: (String) -> Unit
) {
    // Estado para saber se está expandido ou recolhido
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Cabeçalho (Header) que clica
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderColor)
                .clickable { expanded = !expanded } // Troca o estado ao clicar
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = TextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            // Ícone que muda dependendo do estado
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                contentDescription = if (expanded) "Recolher" else "Expandir",
                tint = TextColor
            )
        }

        // Lista de opções (só aparece se expanded for true)
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OptionColor)
                    .padding(8.dp)
            ) {
                options.forEach { (label, code) ->
                    OptionItem(text = label, onClick = { onOptionSelected(code) })
                }
            }
        }
    }
}

@Composable
fun OptionItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = text,
            color = TextHintColor,
            fontSize = 16.sp
        )
    }
}
