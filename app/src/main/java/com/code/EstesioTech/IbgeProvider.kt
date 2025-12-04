package com.code.EstesioTech

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class IbgeState(val id: Int, val sigla: String, val nome: String)

object IbgeProvider {
    // Cache simples para não ficar chamando a API toda hora
    private var cachedStates: List<IbgeState> = emptyList()

    suspend fun getBrazilianStates(): List<IbgeState> {
        if (cachedStates.isNotEmpty()) return cachedStates

        return withContext(Dispatchers.IO) {
            try {
                // URL Oficial do IBGE ordenada por nome
                val url = URL("https://servicodados.ibge.gov.br/api/v1/localidades/estados?orderBy=nome")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val stream = connection.inputStream
                val response = stream.bufferedReader().use { it.readText() }

                val jsonArray = JSONArray(response)
                val states = mutableListOf<IbgeState>()

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    states.add(
                        IbgeState(
                            id = item.getInt("id"),
                            sigla = item.getString("sigla"),
                            nome = item.getString("nome")
                        )
                    )
                }
                // Ordena por Sigla para ficar mais fácil de achar (Opcional, a API já traz por nome)
                cachedStates = states.sortedBy { it.sigla }
                cachedStates
            } catch (e: Exception) {
                Log.e("IBGE", "Erro ao buscar estados: ${e.message}")
                emptyList()
            }
        }
    }
}