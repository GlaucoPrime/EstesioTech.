package com.code.EstesioTech

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // View Holder para mensagens ENVIADAS (Direita, Azul)
    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.tv_chat_message)
    }

    // View Holder para mensagens RECEBIDAS (Esquerda, Laranja)
    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.tv_chat_message)
    }

    // View Holder para mensagens de SISTEMA (Centro, Cinza)
    inner class SystemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.tv_system_message)
    }

    // Decide qual layout usar (enviado, recebido ou sistema)
    override fun getItemViewType(position: Int): Int {
        return messages[position].type
    }

    // Cria o ViewHolder correto para o tipo de layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ChatMessage.TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_sent, parent, false)
                SentViewHolder(view)
            }
            ChatMessage.TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_received, parent, false)
                ReceivedViewHolder(view)
            }
            else -> { // ChatMessage.TYPE_SYSTEM
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_system, parent, false)
                SystemViewHolder(view)
            }
        }
    }

    // Coloca os dados (a mensagem) dentro do ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder.itemViewType) {
            ChatMessage.TYPE_SENT -> {
                (holder as SentViewHolder).messageText.text = message.message
            }
            ChatMessage.TYPE_RECEIVED -> {
                (holder as ReceivedViewHolder).messageText.text = message.message
            }
            ChatMessage.TYPE_SYSTEM -> {
                (holder as SystemViewHolder).messageText.text = message.message
            }
        }
    }

    override fun getItemCount() = messages.size
}