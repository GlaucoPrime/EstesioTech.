package com.code.EstesioTech

// Data class para guardar cada mensagem do chat
data class ChatMessage(
    val message: String,
    val type: Int // Define se é ENVIADA, RECEBIDA ou SISTEMA
) {
    companion object {
        const val TYPE_SENT = 0
        const val TYPE_RECEIVED = 1
        const val TYPE_SYSTEM = 2 // Para "Conectado", "Desconectado"
    }
}