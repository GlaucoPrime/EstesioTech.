package com.code.EstesioTech

import androidx.compose.ui.graphics.Color

data class ClinicalResult(
    val level: Int,
    val color: Color,
    val force: String,
    val description: String,
    val interpretation: String
)

object ClinicalScale {
    fun getResult(level: Int): ClinicalResult {
        return when (level) {
            1 -> ClinicalResult(1, Color(0xFF4CAF50), "0,05 g", "Sensibilidade normal", "Nenhuma perda protetora.") // Verde
            2 -> ClinicalResult(2, Color(0xFF2196F3), "0,2 g", "Sensibilidade diminuída", "Perda leve, ainda protege.") // Azul
            3 -> ClinicalResult(3, Color(0xFF9C27B0), "2,0 g", "Sensibilidade diminuída", "Perda protetora leve.") // Violeta
            4 -> ClinicalResult(4, Color(0xFFF44336), "4,0 g", "Perda da sensibilidade", "Risco de lesões.") // Vermelho
            5 -> ClinicalResult(5, Color(0xFFFF9800), "10,0 g", "Perda da sensibilidade", "Alto risco de úlceras.") // Laranja
            6, 7 -> ClinicalResult(6, Color(0xFFE91E63), "300,0 g", "Perda total / Dor", "Apenas dor profunda.") // Magenta
            else -> ClinicalResult(0, Color.Gray, "-", "Aguardando...", "Toque para iniciar")
        }
    }
}