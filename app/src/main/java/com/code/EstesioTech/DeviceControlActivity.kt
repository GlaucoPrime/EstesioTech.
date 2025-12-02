package com.code.EstesioTech

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Home // Novo ícone para Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.code.EstesioTech.ui.theme.EstesioTechTheme
import com.code.EstesioTech.BleManager

class DeviceControlActivity : ComponentActivity(), BleManager.ConnectionListener {

    private val messages = mutableStateListOf<ChatMessage>()
    // Flag para saber se estamos voltando para Home mantendo a conexão
    private var keepConnectionAlive = false

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BleManager.setListener(this)
        val address = intent.getStringExtra("DEVICE_ADDRESS")

        if (address != null) {
            BleManager.connectToDevice(address, this)
            addMessage("Sistema: Conectando a $address...", ChatMessage.TYPE_SYSTEM)
        } else if (BleManager.isConnected()) {
            addMessage("Sistema: Conexão reestabelecida.", ChatMessage.TYPE_SYSTEM)
        } else {
            addMessage("Sistema: Erro - Sem endereço.", ChatMessage.TYPE_SYSTEM)
        }

        setContent {
            EstesioTechTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Chat ESP32", color = Color.White) },
                            navigationIcon = {
                                IconButton(onClick = {
                                    // Voltar normal (desconecta se não configurar diferente)
                                    finish()
                                }) {
                                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                                }
                            },
                            actions = {
                                // Botão HOME: Volta para o menu principal mas MANTÉM a conexão
                                IconButton(onClick = {
                                    keepConnectionAlive = true
                                    val intent = Intent(this@DeviceControlActivity, HomeActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    startActivity(intent)
                                    finish()
                                }) {
                                    Icon(Icons.Default.Home, "Menu Principal", tint = Color.White)
                                }

                                // Botão DESCONECTAR
                                IconButton(onClick = {
                                    BleManager.disconnect()
                                    finish()
                                }) {
                                    Icon(Icons.Default.BluetoothDisabled, "Desconectar", tint = Color.White)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF1A242E)
                            )
                        )
                    }
                ) { paddingValues ->
                    ChatScreen(
                        messages = messages,
                        onSendMessage = { text ->
                            BleManager.write(text)
                            addMessage("Você: $text", ChatMessage.TYPE_SENT)
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }

    private fun addMessage(text: String, type: Int) {
        messages.add(ChatMessage(text, type))
    }

    override fun onConnected() {
        runOnUiThread { addMessage("Sistema: Conectado!", ChatMessage.TYPE_SYSTEM) }
    }

    override fun onDisconnected() {
        runOnUiThread { addMessage("Sistema: Desconectado", ChatMessage.TYPE_SYSTEM) }
    }

    override fun onDataReceived(data: String) {
        runOnUiThread { addMessage(data, ChatMessage.TYPE_RECEIVED) }
    }

    override fun onError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            addMessage("Sistema: $message", ChatMessage.TYPE_SYSTEM)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.setListener(null)
        // Só desconecta se NÃO estivermos indo para a Home intencionalmente
        if (!keepConnectionAlive) {
            BleManager.disconnect()
        }
    }
}

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(msg)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite a mensagem") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val alignment = when (message.type) {
        ChatMessage.TYPE_SENT -> Alignment.CenterEnd
        ChatMessage.TYPE_RECEIVED -> Alignment.CenterStart
        else -> Alignment.Center
    }

    val backgroundColor = when (message.type) {
        ChatMessage.TYPE_SENT -> MaterialTheme.colorScheme.primary
        ChatMessage.TYPE_RECEIVED -> MaterialTheme.colorScheme.tertiary
        else -> Color(0x33FFFFFF)
    }

    val shape = when (message.type) {
        ChatMessage.TYPE_SENT -> RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
        ChatMessage.TYPE_RECEIVED -> RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
        else -> RoundedCornerShape(12.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            color = backgroundColor,
            shape = shape,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = message.message,
                color = if (message.type == ChatMessage.TYPE_SYSTEM) Color.LightGray else Color.White,
                modifier = Modifier.padding(12.dp),
                fontSize = if (message.type == ChatMessage.TYPE_SYSTEM) 12.sp else 16.sp
            )
        }
    }
}