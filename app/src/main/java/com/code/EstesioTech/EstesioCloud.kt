package com.code.EstesioTech

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

object EstesioCloud {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private const val APP_DOMAIN = "@estesiotech.app"

    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // --- LOGIN COM CRM + UF ---
    fun login(crm: String, uf: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val cleanCrm = crm.trim()

        if (cleanCrm.isEmpty() || uf.isEmpty()) {
            onError("CRM e Estado são obrigatórios.")
            return
        }

        // Agora o email é composto pelo CRM + UF (Ex: 12345_PE@estesiotech.app)
        val fakeEmail = "${cleanCrm}_${uf}$APP_DOMAIN"

        auth.signInWithEmailAndPassword(fakeEmail, pass)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                val msg = when {
                    e.message?.contains("user-not-found") == true -> "Médico não encontrado neste Estado."
                    e.message?.contains("wrong-password") == true -> "Senha incorreta."
                    else -> "Erro: ${e.message}"
                }
                onError(msg)
            }
    }

    // --- REGISTRO COM CRM + UF ---
    fun register(crm: String, uf: String, pass: String, name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val cleanCrm = crm.trim()

        if (cleanCrm.isEmpty() || uf.isEmpty() || name.isEmpty() || pass.isEmpty()) {
            onError("Todos os campos são obrigatórios.")
            return
        }

        val fakeEmail = "${cleanCrm}_${uf}$APP_DOMAIN"

        auth.createUserWithEmailAndPassword(fakeEmail, pass)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid
                if (userId != null) {
                    val userMap = hashMapOf(
                        "name" to name,
                        "crm" to cleanCrm,
                        "uf" to uf, // Salvamos a UF no banco também
                        "role" to "profissional_saude",
                        "createdAt" to Date()
                    )

                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onSuccess() }
                }
            }
            .addOnFailureListener { e ->
                val msg = when {
                    e.message?.contains("email-already-in-use") == true -> "Este CRM já existe neste Estado."
                    e.message?.contains("weak-password") == true -> "Senha fraca (mínimo 6 dígitos)."
                    else -> "Erro: ${e.message}"
                }
                onError(msg)
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun saveTestResult(
        bodyPart: String,
        results: Map<Int, Int>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Usuário desconectado.")
        val validValues = results.values.filter { it > 0 }
        val average = if (validValues.isNotEmpty()) validValues.average() else 0.0

        val testMap = hashMapOf(
            "userId" to userId,
            "bodyPart" to bodyPart,
            "date" to Date(),
            "averageLevel" to average,
            "pointsData" to results,
            "status" to "completed"
        )

        db.collection("tests").add(testMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro ao salvar") }
    }
}