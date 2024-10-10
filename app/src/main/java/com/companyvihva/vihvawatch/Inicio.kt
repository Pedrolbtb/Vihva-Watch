package com.companyvihva.vihvawatch.Inicio

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
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import com.companyvihva.vihvawatch.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Inicio : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var measureClient: MeasureClient
    private lateinit var passiveMonitoringClient: PassiveMonitoringClient
    private lateinit var textBemVindo: TextView
    private var totalSteps: Int = 0  // Campo para armazenar o total de andares

    companion object {
        private const val REQUEST_CODE = 1001  // Defina o REQUEST_CODE aqui
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        db = FirebaseFirestore.getInstance()
        measureClient = HealthServices.getClient(this).measureClient
        passiveMonitoringClient = HealthServices.getClient(this).passiveMonitoringClient

        textBemVindo = findViewById(R.id.titulo_login)  // Inicializa o TextView aqui

        // Verifica e solicita permissão de reconhecimento de atividades
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_CODE
            )
        } else {
            // Permissão já concedida, você pode prosseguir para acessar os dados de andares
            fetchUserData()
            startHealthDataListener()
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

                        val textoCompleto =
                            "Bem-vindo, $nome! Você está conectado ao Vihva Watch, que usará os dados de exercícios físicos no seu aplicativo principal."

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, você pode prosseguir para acessar os dados de andares
                fetchUserData()
                startHealthDataListener()
            } else {
                Log.d("BemVindoActivity", "Permissão de reconhecimento de atividades negada.")
            }
        }
    }

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

    private fun startHealthDataListener() {
        // Configurar o listener para obter dados de saúde (como andares)
        val passiveListenerConfig = PassiveListenerConfig.Builder()
            .setDataTypes(setOf(DataType.DISTANCE_DAILY)) // Utilizando FLOORS_TOTAL
            .build()

        // Callback para lidar com os dados recebidos
        val callback = object : PassiveListenerCallback {
            fun onNewDataReceived(dataPoints: List<DataPoint<*>>) {
                dataPoints.forEach { dataPoint ->
                    if (dataPoint.dataType == DataType.DISTANCE_DAILY) {
                        // Aqui, acesse o número de andares de uma maneira diferente
                        val floors = when (dataPoint.dataType) {
                            DataType.DISTANCE_DAILY -> (dataPoint.to(DataType.DISTANCE_DAILY) as? Int) ?: 0
                            else -> 0
                        }

                        totalSteps += floors
                        Log.d("HealthData", "Número de andares: $floors")
                        updateStepCount(totalSteps)
                    }
                }
            }

            override fun onRegistered() {
                Log.d("HealthData", "Listener de saúde registrado.")
            }

            override fun onRegistrationFailed(throwable: Throwable) {
                Log.e("HealthData", "Falha ao registrar o listener.", throwable)
            }
        }

        // Registra o ouvinte para os dados de saúde
        passiveMonitoringClient.setPassiveListenerCallback(passiveListenerConfig, callback)
    }




    private fun updateStepCount(floors: Int) {
        val stepCountMessage = "Total de andares: $floors" // Mensagem atualizada para andares
        textBemVindo.text = stepCountMessage
    }
}