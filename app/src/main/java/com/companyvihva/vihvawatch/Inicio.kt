package com.companyvihva.vihvawatch.Inicio

import android.content.pm.PackageManager
import android.graphics.Typeface
import android.health.connect.HealthConnectException
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.companyvihva.vihvawatch.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Inicio : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore



    companion object {
        private const val REQUEST_CODE = 1001  // Defina o REQUEST_CODE aqui
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        db = FirebaseFirestore.getInstance()


        // Verifica e solicita permissão de reconhecimento de atividades
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_CODE)
        } else {
            // Permissão já concedida, você pode prosseguir para acessar os dados de passos
            fetchUserData()
        }
    }

    // Método para buscar dados do usuário
    private fun fetchUserData() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        currentUserUid?.let { uid ->
            val userDocRef = db.collection("clientes").document(uid)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nome = document.getString("nome") ?: "Nome não fornecido"
                        val textBemVindo = findViewById<TextView>(R.id.titulo_login)

                        val textoCompleto = "Bem-vindo, $nome! Você está conectado ao Vihva Watch, que usará os dados de exercícios físicos no seu aplicativo principal."

                        // Cria um SpannableString
                        val spannableString = SpannableString(textoCompleto)

                        // Define a parte "Vihva Watch" para ter uma fonte diferente
                        val startIndex = textoCompleto.indexOf("Vihva Watch")
                        val endIndex = startIndex + "Vihva Watch".length

                        // Carrega a fonte personalizada
                        val customTypeface = ResourcesCompat.getFont(this, R.font.peanut_butter)

                        // Verifica se a fonte foi carregada com sucesso
                        if (customTypeface != null) {
                            // Aplica a fonte personalizada à parte "Vihva Watch"
                            spannableString.setSpan(
                                CustomTypefaceSpan(customTypeface),
                                startIndex,
                                endIndex,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        } else {
                            Log.d("BemVindoActivity", "Fonte personalizada não foi carregada.")
                        }

                        textBemVindo.text = spannableString
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("BemVindoActivity", "Erro ao pegar o documento: ${exception.message}")
                }
        }
    }

    // Método que lida com a resposta da solicitação de permissão
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permissão concedida, você pode prosseguir para acessar os dados de passos
                fetchUserData()
            } else {
                Log.d("BemVindoActivity", "Permissão de reconhecimento de atividades negada.")
            }
        }
    }

    // Classe para aplicar a fonte personalizada
    class CustomTypefaceSpan(private val newType: Typeface) : StyleSpan(Typeface.NORMAL) {
        override fun updateDrawState(ds: android.text.TextPaint) {
            super.updateDrawState(ds)
            ds.typeface = newType
        }

        override fun updateMeasureState(paint: android.text.TextPaint) {
            super.updateMeasureState(paint)
            paint.typeface = newType
        }
    }
}
