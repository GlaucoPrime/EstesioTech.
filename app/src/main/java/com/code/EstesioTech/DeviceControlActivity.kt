package com.code.EstesioTech

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class DeviceControlActivity : AppCompatActivity(), BleManager.ConnectionListener {

    private lateinit var rvChat: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    private lateinit var etSend: TextInputEditText
    private lateinit var btnSend: MaterialButton
    private lateinit var toolbar: Toolbar

    private var isNavigatingHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)

        // Configura a Toolbar
        toolbar = findViewById(R.id.toolbar_device_control)
        setSupportActionBar(toolbar)

        etSend = findViewById(R.id.edit_send)
        btnSend = findViewById(R.id.btn_send_chat)

        // Configura o RecyclerView
        rvChat = findViewById(R.id.rv_chat)
        chatAdapter = ChatAdapter(messageList)
        rvChat.adapter = chatAdapter
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Faz a lista "crescer" de baixo para cima
        }

        BleManager.setListener(this)

        // Pega o endereço passado pela MainActivity
        val address = intent.getStringExtra("DEVICE_ADDRESS")

        if (address != null) {
            // Este é o fluxo normal de conexão vindo do Scan
            BleManager.connectToDevice(address, this)
            addMessageToChat("Sistema: Conectando a $address...", ChatMessage.TYPE_SYSTEM)
        } else {
            // Este é o fluxo se voltarmos da HomeActivity
            if (BleManager.isConnected()) {
                addMessageToChat("Sistema: Conexão reestabelecida.", ChatMessage.TYPE_SYSTEM)
            } else {
                addMessageToChat("Sistema: Erro - Endereço do dispositivo não fornecido.", ChatMessage.TYPE_SYSTEM)
            }
        }

        btnSend.setOnClickListener {
            val txt = etSend.text.toString()
            if (txt.isNotBlank()) {
                BleManager.write(txt)
                addMessageToChat("Você: $txt", ChatMessage.TYPE_SENT)
                etSend.setText("")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.device_control_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home -> {
                isNavigatingHome = true
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
                return true
            }
            R.id.menu_disconnect -> {
                BleManager.disconnect()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // --- Callbacks do BleManager ---

    override fun onConnected() {
        runOnUiThread {
            addMessageToChat("Sistema: Conectado!", ChatMessage.TYPE_SYSTEM)
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            addMessageToChat("Sistema: Desconectado", ChatMessage.TYPE_SYSTEM)
        }
    }

    override fun onDataReceived(data: String) {
        runOnUiThread {
            addMessageToChat(data, ChatMessage.TYPE_RECEIVED)
        }
    }

    override fun onError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            addMessageToChat("Sistema: $message", ChatMessage.TYPE_SYSTEM)
        }
    }

    private fun addMessageToChat(text: String, type: Int) {
        messageList.add(ChatMessage(text, type))
        chatAdapter.notifyItemInserted(messageList.size - 1)
        rvChat.scrollToPosition(messageList.size - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.setListener(null)
        if (!isNavigatingHome) {
            BleManager.disconnect()
        }
    }
}