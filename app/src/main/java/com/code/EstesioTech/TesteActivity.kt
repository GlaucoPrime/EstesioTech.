package com.code.EstesioTech

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha // ✅ O IMPORT QUE SALVA TUDO
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.code.EstesioTech.ui.theme.EstesioTechTheme

class TesteActivity : ComponentActivity(), BleManager.ConnectionListener {

    private val activePointIndex = mutableIntStateOf(-1)
    private val currentBleValue = mutableIntStateOf(0)
    private var lastValidValue = 0

    private val resultsMap = mutableStateMapOf<Int, Int>()
    private val isBleConnected = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceAddress = intent.getStringExtra("DEVICE_ADDRESS")
        val bodyPart = intent.getStringExtra("BODY_PART") ?: "mao_direita"

        BleManager.setListener(this)
        isBleConnected.value = BleManager.isConnected()

        if (deviceAddress != null && !BleManager.isConnected()) {
            BleManager.connectToDevice(deviceAddress, this)
        }

        setContent {
            EstesioTechTheme {
                TesteScreen(
                    bodyPart = bodyPart,
                    results = resultsMap,
                    activePointIndex = activePointIndex.intValue,
                    currentBleValue = currentBleValue.intValue,
                    isConnected = isBleConnected.value,
                    onPointSelect = { index ->
                        activePointIndex.intValue = index
                        currentBleValue.intValue = 0
                        lastValidValue = 0
                    },
                    onCloseMeasurement = {
                        activePointIndex.intValue = -1
                    },
                    onBackClick = { finish() },
                    onResetPoint = { index ->
                        resultsMap.remove(index)
                    }
                )
            }
        }
    }

    override fun onDataReceived(data: String) {
        val cleanData = data.trim()

        if (cleanData.equals("Enviado", ignoreCase = true)) {
            val finalValue = lastValidValue
            val currentIndex = activePointIndex.intValue

            if (currentIndex != -1 && finalValue > 0) {
                runOnUiThread {
                    resultsMap[currentIndex] = finalValue
                    activePointIndex.intValue = -1
                    Toast.makeText(this, "Salvo: Nível $finalValue", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val value = cleanData.toIntOrNull()
            if (value != null) {
                if (value > 0) {
                    lastValidValue = value
                    runOnUiThread { currentBleValue.intValue = value }
                } else if (value == 0) {
                    runOnUiThread { currentBleValue.intValue = 0 }
                }
            }
        }
    }

    override fun onConnected() { runOnUiThread { isBleConnected.value = true } }
    override fun onDisconnected() { runOnUiThread { isBleConnected.value = false } }
    override fun onError(message: String) {}

    override fun onDestroy() {
        super.onDestroy()
        BleManager.setListener(null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TesteScreen(
    bodyPart: String,
    results: Map<Int, Int>,
    activePointIndex: Int,
    currentBleValue: Int,
    isConnected: Boolean,
    onPointSelect: (Int) -> Unit,
    onCloseMeasurement: () -> Unit,
    onBackClick: () -> Unit,
    onResetPoint: (Int) -> Unit
) {
    val title = when(bodyPart) {
        "mao_direita" -> "Mão Direita"
        "mao_esquerda" -> "Mão Esquerda"
        "pe_direito" -> "Pé Direito"
        "pe_esquerdo" -> "Pé Esquerdo"
        else -> "Teste Clínico"
    }

    val imageRes = when(bodyPart) {
        "mao_direita" -> R.drawable.right_hand
        "mao_esquerda" -> R.drawable.left_hand
        "pe_direito" -> R.drawable.right_foot
        "pe_esquerdo" -> R.drawable.left_foot
        else -> R.drawable.right_hand
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if(isConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                                contentDescription = null,
                                tint = if(isConnected) Color.Green else Color.Red,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if(isConnected) "Conectado" else "Desconectado",
                                color = if(isConnected) Color.Green else Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF101820))
            )
        },
        containerColor = Color(0xFF101820)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().alpha(0.9f)
                )

                // =========================================================================
                // MAPEAMENTO MANUAL DE CADA MEMBRO (Baseado nas suas imagens)
                // =========================================================================

                when(bodyPart) {
                    "mao_direita" -> {
                        // Mão Direita (Dedão na Direita, Mindinho na Esquerda)
                        MedicalPoint(0, 0.90f, 0.35f, results[0], onPointSelect) // Dedão
                        MedicalPoint(1, 0.70f, 0.22f, results[1], onPointSelect) // Indicador
                        MedicalPoint(2, 0.50f, 0.15f, results[2], onPointSelect) // Médio
                        MedicalPoint(3, 0.30f, 0.22f, results[3], onPointSelect) // Anelar
                        MedicalPoint(4, 0.10f, 0.35f, results[4], onPointSelect) // Mindinho
                        MedicalPoint(5, 0.20f, 0.60f, results[5], onPointSelect) // Palma (Lado Mindinho)
                        MedicalPoint(6, 0.80f, 0.65f, results[6], onPointSelect) // Palma (Lado Dedão)
                    }
                    "mao_esquerda" -> {
                        // Mão Esquerda (Dedão na Esquerda, Mindinho na Direita)
                        MedicalPoint(0, 0.10f, 0.35f, results[0], onPointSelect) // Dedão
                        MedicalPoint(1, 0.30f, 0.22f, results[1], onPointSelect) // Indicador
                        MedicalPoint(2, 0.50f, 0.15f, results[2], onPointSelect) // Médio
                        MedicalPoint(3, 0.70f, 0.22f, results[3], onPointSelect) // Anelar
                        MedicalPoint(4, 0.90f, 0.35f, results[4], onPointSelect) // Mindinho
                        MedicalPoint(5, 0.80f, 0.60f, results[5], onPointSelect) // Palma (Lado Mindinho)
                        MedicalPoint(6, 0.20f, 0.65f, results[6], onPointSelect) // Palma (Lado Dedão)
                    }
                    "pe_direito" -> {
                        // Pé Direito (Dedão na Esquerda)
                        MedicalPoint(0, 0.35f, 0.08f, results[0], onPointSelect) // Dedão
                        MedicalPoint(1, 0.55f, 0.12f, results[1], onPointSelect) // Dedo 2
                        MedicalPoint(2, 0.85f, 0.20f, results[2], onPointSelect) // Mindinho

                        MedicalPoint(3, 0.30f, 0.30f, results[3], onPointSelect) // Almofada Dedão
                        MedicalPoint(4, 0.60f, 0.30f, results[4], onPointSelect) // Almofada Meio
                        MedicalPoint(5, 0.85f, 0.35f, results[5], onPointSelect) // Almofada Mindinho

                        MedicalPoint(6, 0.30f, 0.60f, results[6], onPointSelect) // Arco
                        MedicalPoint(7, 0.75f, 0.55f, results[7], onPointSelect) // Lateral

                        MedicalPoint(8, 0.50f, 0.85f, results[8], onPointSelect) // Calcanhar
                    }
                    "pe_esquerdo" -> {
                        // Pé Esquerdo (Dedão na Direita) - Espelho do Direito
                        MedicalPoint(0, 0.65f, 0.08f, results[0], onPointSelect) // Dedão
                        MedicalPoint(1, 0.45f, 0.12f, results[1], onPointSelect) // Dedo 2
                        MedicalPoint(2, 0.15f, 0.20f, results[2], onPointSelect) // Mindinho

                        MedicalPoint(3, 0.70f, 0.30f, results[3], onPointSelect) // Almofada Dedão
                        MedicalPoint(4, 0.40f, 0.30f, results[4], onPointSelect) // Almofada Meio
                        MedicalPoint(5, 0.15f, 0.35f, results[5], onPointSelect) // Almofada Mindinho

                        MedicalPoint(6, 0.70f, 0.60f, results[6], onPointSelect) // Arco
                        MedicalPoint(7, 0.25f, 0.55f, results[7], onPointSelect) // Lateral

                        MedicalPoint(8, 0.50f, 0.85f, results[8], onPointSelect) // Calcanhar
                    }
                }
            }

            val filledValues = results.values.filter { it > 0 }
            val averageLevel = if (filledValues.isNotEmpty())
                filledValues.average().let { kotlin.math.round(it).toInt() }
            else 0
            val globalResult = ClinicalScale.getResult(averageLevel)

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A242E))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(globalResult.color)
                            .border(2.dp, Color.White, CircleShape), // Borda no Global também
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if(averageLevel > 0) "$averageLevel" else "-", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Média Global", color = Color.Gray, fontSize = 12.sp)
                        Text(globalResult.description, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (activePointIndex != -1) {
            MeasurementDialog(
                currentLevel = currentBleValue,
                onDismiss = onCloseMeasurement
            )
        }
    }
}

@Composable
fun BoxWithConstraintsScope.MedicalPoint(
    index: Int,
    xPercent: Float,
    yPercent: Float,
    resultLevel: Int?,
    onClick: (Int) -> Unit
) {
    val isDone = resultLevel != null
    val resultData = ClinicalScale.getResult(resultLevel ?: 0)

    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha"
    )

    // Cor Padrão (Laranja do seu XML se não tiver resultado, ou a Cor Clínica se tiver)
    val baseColor = if (isDone) resultData.color else Color(0xFFFF9800)

    // Se não estiver feito, pisca. Se estiver feito, fica sólido.
    val finalAlpha = if (isDone) 1f else alphaAnim

    Box(
        modifier = Modifier
            // Ajuste fino: usamos as porcentagens para centralizar o ponto de 30dp
            .offset(
                x = maxWidth * xPercent - 15.dp,
                y = maxHeight * yPercent - 15.dp
            )
            .size(30.dp) // Tamanho igual ao seu XML
            .clip(CircleShape)
            .border(2.dp, Color.White, CircleShape)
            .background(baseColor.copy(alpha = if (isDone) 1f else 0.5f))
            .clickable { onClick(index) },
        contentAlignment = Alignment.Center
    ) {
        // Miolo da bolinha
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(baseColor)
                .alpha(finalAlpha) // Pisca o miolo
        )
        if (isDone) {
            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun MeasurementDialog(
    currentLevel: Int,
    onDismiss: () -> Unit
) {
    val data = ClinicalScale.getResult(currentLevel)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A242E))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Medindo...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(data.color)
                        .border(4.dp, Color.White, CircleShape), // Borda no popup
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if(currentLevel == 0) "?" else "$currentLevel",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if(currentLevel == 0) "..." else data.force,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(data.description, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }

                Text(
                    "Aperte RESET no aparelho para SALVAR.",
                    color = Color.Yellow,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}