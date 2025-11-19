package com.code.EstesioTech

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var connectDeviceButton: Button
    private lateinit var startTestButton: Button
    private lateinit var testHistoryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        connectDeviceButton = findViewById(R.id.connectDeviceButton)
        startTestButton = findViewById(R.id.startTestButton)
        testHistoryButton = findViewById(R.id.testHistoryButton)

        // Botão CONECTAR (leva ao Scan)
        connectDeviceButton.setOnClickListener {
            if (BleManager.isConnected()) {
                Toast.makeText(this, "Você já está conectado.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        // Botão INICIAR TESTE (leva ao Placeholder)
        startTestButton.setOnClickListener {
            // Este botão só é clicável se estivermos conectados,
            // mas o levamos para a tela placeholder
            val intent = Intent(this, TesteActivity::class.java)
            startActivity(intent)
        }

        // Botão HISTÓRICO (Toast)
        testHistoryButton.setOnClickListener {
            Toast.makeText(this, "Funcionalidade de histórico ainda não implementada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza o estado dos botões sempre que a tela aparece
        updateTestButtonState()
    }

    private fun updateTestButtonState() {
        // Habilita/Desabilita o botão Iniciar Teste
        startTestButton.isEnabled = BleManager.isConnected()

        // Habilita/Desabilita o botão Conectar
        connectDeviceButton.isEnabled = !BleManager.isConnected()
    }
}