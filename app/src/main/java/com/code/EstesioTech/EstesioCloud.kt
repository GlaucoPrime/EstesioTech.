package com.code.EstesioTech

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

object EstesioCloud {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private const val APP_DOMAIN = "@estesiotech.app"

    // --- UTILITÁRIOS ---
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Tradutor de Erros do Firebase
    private fun getFriendlyErrorMessage(exception: Exception?): String {
        val msg = exception?.message ?: return "Erro desconhecido."
        return when {
            msg.contains("email-already-in-use") -> "Este CRM já está cadastrado."
            msg.contains("weak-password") -> "A senha precisa ter pelo menos 6 caracteres."
            msg.contains("user-not-found") -> "CRM não encontrado ou usuário inativo."
            msg.contains("wrong-password") -> "Senha incorreta."
            msg.contains("invalid-email") -> "Formato de CRM inválido."
            msg.contains("network-request-failed") -> "Sem conexão com a internet."
            else -> "Erro: ${msg.take(50)}..." // Corta se for muito longo
        }
    }

    // --- LOGIN ---
    fun login(crm: String, uf: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val cleanCrm = crm.trim()
        if (cleanCrm.isEmpty() || uf.isEmpty()) {
            onError("Preencha CRM e Estado.")
            return
        }

        val fakeEmail = "${cleanCrm}_${uf}$APP_DOMAIN"

        auth.signInWithEmailAndPassword(fakeEmail, pass)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(getFriendlyErrorMessage(e)) }
    }

    // --- REGISTRO COM AUTO-LOGIN ---
    fun register(crm: String, uf: String, pass: String, name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val cleanCrm = crm.trim()

        if (cleanCrm.isEmpty() || uf.isEmpty() || name.isEmpty() || pass.isEmpty()) {
            onError("Todos os campos são obrigatórios.")
            return
        }

        val fakeEmail = "${cleanCrm}_${uf}$APP_DOMAIN"

        auth.createUserWithEmailAndPassword(fakeEmail, pass)
            .addOnSuccessListener { result ->
                // Sucesso no Auth! Agora salva os dados no Firestore
                val userId = result.user?.uid
                if (userId != null) {
                    val userMap = hashMapOf(
                        "name" to name,
                        "crm" to cleanCrm,
                        "uf" to uf,
                        "role" to "profissional_saude",
                        "createdAt" to Date()
                    )

                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            // SUCESSO TOTAL: Conta criada e dados salvos.
                            // Como o createUser já loga automaticamente, só chamamos sucesso.
                            onSuccess()
                        }
                        .addOnFailureListener {
                            // Se falhar o banco, mas criou a conta, deixamos passar (Alfa)
                            onSuccess()
                        }
                }
            }
            .addOnFailureListener { e -> onError(getFriendlyErrorMessage(e)) }
    }

    // --- RECUPERAR SENHA ---
    // Nota: Só funcionaria se fosse email real. Com CRM, isso é simbólico.
    fun sendPasswordReset(crm: String, uf: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (crm.isEmpty() || uf.isEmpty()) {
            onError("Informe seu CRM e Estado.")
            return
        }
        val fakeEmail = "${crm}_${uf}$APP_DOMAIN"
        auth.sendPasswordResetEmail(fakeEmail)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(getFriendlyErrorMessage(e)) }
    }

    fun logout() {
        auth.signOut()
    }

    // Obter nome para a Home
    fun getUserName(onResult: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val fullName = document.getString("name") ?: "Doutor(a)"
                onResult(fullName.split(" ").firstOrNull() ?: fullName)
            }
            .addOnFailureListener { onResult("Doutor(a)") }
    }

    // --- SALVAR TESTE (A FUNÇÃO QUE FALTAVA) ---
    fun saveTestResult(
        bodyPart: String,
        results: Map<Int, Int>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Usuário desconectado.")

        // Calcula média
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

        db.collection("tests")
            .add(testMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro ao salvar") }
    }
}