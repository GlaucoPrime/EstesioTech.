package com.code.EstesioTech

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TesteActivity : AppCompatActivity() {

    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teste)

        backButton = findViewById(R.id.back_to_home_button)

        backButton.setOnClickListener {
            finish() // Apenas fecha a tela e volta para a Home
        }
    }
}