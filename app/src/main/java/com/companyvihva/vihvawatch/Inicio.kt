package com.companyvihva.vihvawatch.Inicio

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
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
import com.companyvihva.vihvawatch.services.FitnessService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Inicio : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var textBemVindo: TextView

    companion object {
        private const val REQUEST_CODE = 1001  // Código de requisição para permissões
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        // Inicializa o Firestore
        db = FirebaseFirestore.getInstance()

        // Inicializa a TextView para o texto de boas-vindas
        textBemVindo = findViewById(R.id.titulo_login)

        // Verifica e solicita permissões necessárias
        checkPermissionsAndStartService()
    }

    private fun checkPermissionsAndStartService() {
        // Verifica se as permissões de reconhecimento de atividades e sensores corporais foram concedidas
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            // Se não concedidas, solicita as permissões
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACTIVITY_RECOGNITION,
                    android.Manifest.permission.BODY_SENSORS
                ),
                REQUEST_CODE
            )
        } else {
            // Permissões já concedidas, inicia a coleta de dados
            fetchUserData()
            startFitnessService()
        }
    }

    private fun fetchUserData() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        currentUserUid?.let { uid ->
            val userDocRef = db.collection("clientes").document(uid)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nome = document.getString("nome") ?: "Nome não fornecido"
                        val textoCompleto = "Bem-vindo, $nome! Você está conectado ao Vihva Watch, que usará os dados de exercícios físicos no seu aplicativo principal."

                        // Cria um SpannableString para aplicar fonte personalizada
                        val spannableString = SpannableString(textoCompleto)
                        val startIndex = textoCompleto.indexOf("Vihva Watch")
                        val endIndex = startIndex + "Vihva Watch".length
                        val customTypeface = ResourcesCompat.getFont(this, R.font.peanut_butter)

                        // Aplica a fonte personalizada se estiver disponível
                        if (customTypeface != null) {
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

    // Inicia o serviço de coleta de dados de fitness
    private fun startFitnessService() {
        val serviceIntent = Intent(this, FitnessService::class.java)
        startService(serviceIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissões concedidas, iniciar coleta de dados
                fetchUserData()
                startFitnessService()
            } else {
                Log.d("BemVindoActivity", "Permissão de reconhecimento de atividades ou sensores negada.")
            }
        }
    }

    // Classe interna para customizar fonte no SpannableString
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
