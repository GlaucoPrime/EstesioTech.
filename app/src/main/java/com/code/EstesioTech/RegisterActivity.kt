package com.code.EstesioTech

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerNameInput: TextInputEditText
    private lateinit var registerLicenseInput: TextInputEditText
    private lateinit var registerPasswordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var clearRegisterButton: MaterialButton
    private lateinit var loginText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerNameInput = findViewById(R.id.registerNameInput)
        registerLicenseInput = findViewById(R.id.registerLicenseInput)
        registerPasswordInput = findViewById(R.id.registerPasswordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        registerButton = findViewById(R.id.registerButton)
        clearRegisterButton = findViewById(R.id.clearRegisterButton)
        loginText = findViewById(R.id.loginText)

        registerButton.setOnClickListener {
            val name = registerNameInput.text.toString()
            val license = registerLicenseInput.text.toString()
            val password = registerPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (name.isEmpty() || license.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
            else if (password != confirmPassword) {
                Toast.makeText(this, "As senhas não conferem!", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        clearRegisterButton.setOnClickListener {
            registerNameInput.text?.clear()
            registerLicenseInput.text?.clear()
            registerPasswordInput.text?.clear()
            confirmPasswordInput.text?.clear()
        }

        loginText.setOnClickListener {
            finish()
        }
    }
}