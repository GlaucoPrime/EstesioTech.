package com.code.EstesioTech

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.code.EstesioTech.ui.theme.EstesioTechTheme
import com.code.EstesioTech.BleManager
import com.code.EstesioTech.R

class TesteActivity : ComponentActivity(), BleManager.ConnectionListener {

    private val currentPointIndex = mutableIntStateOf(0)
    private val testFinished = mutableStateOf(false)
    private val pointsStatus = mutableStateListOf(false, false, false, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BleManager.setListener(this)
        val address = intent.getStringExtra("DEVICE_ADDRESS")
        if (address != null) {
            BleManager.connectToDevice(address, this)
        }

        setContent {
            EstesioTechTheme {
                TesteScreen(
                    currentPointIndex = currentPointIndex.intValue,
                    isTestFinished = testFinished.value,
                    pointsStatus = pointsStatus,
                    onPointClick = { index ->
                        if (!testFinished.value && index == currentPointIndex.intValue) {
                            registerPoint(index, "Toque Manual")
                        }
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }

    private fun registerPoint(index: Int, value: String) {
        if (index < pointsStatus.size) pointsStatus[index] = true
        Toast.makeText(this, "Ponto ${index + 1}: $value", Toast.LENGTH_SHORT).show()

        if (currentPointIndex.intValue < 3) {
            currentPointIndex.intValue += 1
        } else {
            testFinished.value = true
        }
    }

    override fun onDataReceived(data: String) {
        runOnUiThread {
            if (!testFinished.value) registerPoint(currentPointIndex.intValue, data)
        }
    }

    override fun onConnected() { runOnUiThread { Toast.makeText(this, "Conectado!", Toast.LENGTH_SHORT).show() } }
    override fun onDisconnected() { runOnUiThread { Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show() } }
    override fun onError(message: String) { runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() } }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.setListener(null)
    }
}

@Composable
fun TesteScreen(
    currentPointIndex: Int,
    isTestFinished: Boolean,
    pointsStatus: List<Boolean>,
    onPointClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isTestFinished) "Teste Finalizado!" else "Toque no ponto indicado",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.ic_hand_vector),
                contentDescription = "Mapa da Mão",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(32.dp)
            )

            TestPoint(0, currentPointIndex == 0 && !isTestFinished, pointsStatus[0], (-80).dp, (-50).dp, onPointClick)
            TestPoint(1, currentPointIndex == 1 && !isTestFinished, pointsStatus[1], (-30).dp, (-130).dp, onPointClick)
            TestPoint(2, currentPointIndex == 2 && !isTestFinished, pointsStatus[2], 20.dp, (-135).dp, onPointClick)
            TestPoint(3, currentPointIndex == 3 && !isTestFinished, pointsStatus[3], 0.dp, 30.dp, onPointClick)
        }

        TextButton(onClick = onBackClick, modifier = Modifier.padding(bottom = 32.dp)) {
            Text("Voltar ao Menu", color = MaterialTheme.colorScheme.secondary, fontSize = 18.sp)
        }
    }
}

@Composable
fun TestPoint(index: Int, isActive: Boolean, isCompleted: Boolean, offsetX: androidx.compose.ui.unit.Dp, offsetY: androidx.compose.ui.unit.Dp, onClick: (Int) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(500), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )
    val currentAlpha = if (isActive) alpha else if (isCompleted) 1.0f else 0.5f
    val currentColor = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFFF9800)

    Box(modifier = Modifier.offset(x = offsetX, y = offsetY).size(40.dp).clip(CircleShape).alpha(currentAlpha).background(currentColor).clickable { onClick(index) })
}