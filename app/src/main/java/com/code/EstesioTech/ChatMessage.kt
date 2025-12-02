package com.code.EstesioTech

data class ChatMessage(
    val message: String,
    val type: Int
) {
    companion object {
        const val TYPE_SENT = 0      // Mensagem enviada por você (Azul, Direita)
        const val TYPE_RECEIVED = 1  // Mensagem recebida da ESP (Laranja, Esquerda)
        const val TYPE_SYSTEM = 2    // Mensagem de sistema (Cinza, Centro)
    }
}