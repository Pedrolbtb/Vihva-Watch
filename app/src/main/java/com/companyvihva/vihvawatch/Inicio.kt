package com.companyvihva.vihvawatch.Inicio

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.companyvihva.vihvawatch.R
import com.companyvihva.vihvawatch.service.FitnessService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Inicio : AppCompatActivity() {
    private lateinit var textBemVindo: TextView
    private lateinit var textPassos: TextView
    private lateinit var textBatimentos: TextView
    private lateinit var textDistancia: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var handlerPassosDistancia: Handler
    private lateinit var handlerBatimentos: Handler
    private lateinit var runnablePassosDistancia: Runnable
    private lateinit var runnableBatimentos: Runnable

    private var isCounting = true
    private var passosContagemFinal = 0
    private var batimentosContagemFinal = 0
    private var distanciaContagemFinal = 0.0

    companion object {
        private const val REQUEST_CODE = 1001
        private const val PASSO_MEDIO_EM_METROS = 0.75
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        // Inicializa FirebaseAuth e Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializa as TextViews para o texto de boas-vindas e dados fitness
        textBemVindo = findViewById(R.id.titulo_login)
        textPassos = findViewById(R.id.text_passos)
        textBatimentos = findViewById(R.id.text_batimentos)
        textDistancia = findViewById(R.id.text_distancia)

        val btnStop: Button = findViewById(R.id.btnStop)
        btnStop.setOnClickListener {
            if (isCounting) {
                stopFitnessData()
                Toast.makeText(this, "Contagem fitness finalizada", Toast.LENGTH_SHORT).show()
            } else {
                startFitnessData()
                Toast.makeText(this, "Contagem fitness reiniciada", Toast.LENGTH_SHORT).show()
            }
            isCounting = !isCounting  // Alterna o estado
        }

        // Exibe mensagem de boas-vindas
        textBemVindo.text = "Vihva Watch"

        // Verifica e solicita permissões necessárias
        checkPermissionsAndStartService()
    }

    private fun checkPermissionsAndStartService() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACTIVITY_RECOGNITION,
                    android.Manifest.permission.BODY_SENSORS
                ),
                REQUEST_CODE
            )
        } else {
            startFitnessService()
        }
    }

    // Atualiza os dados de fitness na interface e armazena para o envio
    private fun updatePassosEDistancia(passos: Int) {
        val distancia = calcularDistancia(passos)
        passosContagemFinal = passos
        distanciaContagemFinal = distancia

        textPassos.text = "Passos: $passos"
        textDistancia.text = "Distância: %.2f Km".format(distancia)  // Atualiza a distância em Km
    }

    private fun updateBatimentos(batimentos: Int) {
        batimentosContagemFinal = batimentos
        textBatimentos.text = "Batimentos: $batimentos BPM"
    }

    // Função para calcular a distância com base nos passos
    private fun calcularDistancia(passos: Int): Double {
        val distanciaMetros = passos * PASSO_MEDIO_EM_METROS
        return distanciaMetros / 1000  // Converte para quilômetros
    }

    private fun startFitnessService() {
        val serviceIntent = Intent(this, FitnessService::class.java)
        startService(serviceIntent)

        simulateFitnessData()
    }

    private fun simulateFitnessData() {
        handlerPassosDistancia = Handler(mainLooper)
        handlerBatimentos = Handler(mainLooper)
        var passos = 0
        var batimentos = 70
        var aumentarBatimentos = true

        // Runnable para atualizar passos e distância a cada 2,5 segundos
        runnablePassosDistancia = object : Runnable {
            override fun run() {
                if (isCounting) {
                    passos += (5..15).random()  // Simula entre 5 e 15 passos por atualização
                    updatePassosEDistancia(passos)
                }
                handlerPassosDistancia.postDelayed(this, 2500)  // Atualiza a cada 2,5 segundos
            }
        }

        // Runnable para atualizar os batimentos cardíacos a cada 1 minuto
        runnableBatimentos = object : Runnable {
            override fun run() {
                if (isCounting) {
                    // Simula a variação de batimentos cardíacos
                    if (aumentarBatimentos) {
                        batimentos += (1..3).random()  // Aumenta os batimentos
                        if (batimentos >= 100) aumentarBatimentos = false  // Se alcançar 100, começa a diminuir
                    } else {
                        batimentos -= (1..3).random()  // Diminui os batimentos
                        if (batimentos <= 60) aumentarBatimentos = true  // Se alcançar 60, começa a aumentar
                    }
                    updateBatimentos(batimentos)
                }
                handlerBatimentos.postDelayed(this, 60000)  // Atualiza a cada 1 minuto (60.000 milissegundos)
            }
        }

        // Inicia ambos os Runnables
        handlerPassosDistancia.post(runnablePassosDistancia)
        handlerBatimentos.post(runnableBatimentos)
    }

    // Função para enviar dados para o Firestore
    private fun enviarDadosParaFirestore(passos: Int, batimentos: Int, distancia: Double) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Cria um mapa com os dados de passos, batimentos e distância
            val dadosFitness = hashMapOf(
                "passos" to passos,
                "batimentos" to batimentos,
                "distancia" to distancia,
                "timestamp" to System.currentTimeMillis() // Adiciona um timestamp
            )

            // Adiciona ou atualiza os dados no documento do usuário na coleção 'clientes'
            db.collection("clientes").document(userId)
                .set(dadosFitness, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("InicioActivity", "Erro ao salvar dados no Firestore", e)
                }
        } else {
            Log.w("InicioActivity", "Usuário não autenticado. Não é possível salvar dados.")
        }
    }

    // Método para parar a contagem dos dados e enviar para o Firestore
    private fun stopFitnessData() {
        handlerPassosDistancia.removeCallbacks(runnablePassosDistancia)
        handlerBatimentos.removeCallbacks(runnableBatimentos)

        // Envia os dados finais para o Firestore
        enviarDadosParaFirestore(passosContagemFinal, batimentosContagemFinal, distanciaContagemFinal)

        Log.d("InicioActivity", "Contagem de dados parada e dados enviados para Firestore.")
    }

    // Método para reiniciar a contagem dos dados
    private fun startFitnessData() {
        simulateFitnessData()
        Log.d("InicioActivity", "Contagem de dados retomada.")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startFitnessService()
            } else {
                Log.d("InicioActivity", "Permissão de reconhecimento de atividades ou sensores negada.")
            }
        }
    }
}
