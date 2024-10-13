package com.companyvihva.vihvawatch.Inicio

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.companyvihva.vihvawatch.R
import com.companyvihva.vihvawatch.service.FitnessService

class Inicio : AppCompatActivity() {
    private lateinit var textBemVindo: TextView
    private lateinit var textPassos: TextView
    private lateinit var textBatimentos: TextView
    private lateinit var textDistancia: TextView

    companion object {
        private const val REQUEST_CODE = 1001
        private const val PASSO_MEDIO_EM_METROS = 0.75
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        // Inicializa as TextViews para o texto de boas-vindas e dados fitness
        textBemVindo = findViewById(R.id.titulo_login)
        textPassos = findViewById(R.id.text_passos)
        textBatimentos = findViewById(R.id.text_batimentos)
        textDistancia = findViewById(R.id.text_distancia)

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

    // Atualiza os dados de fitness na interface
    private fun updatePassosEDistancia(passos: Int) {
        val distancia = calcularDistancia(passos)
        textPassos.text = "Passos: $passos"
        textDistancia.text = "Distância: %.2f Km".format(distancia)  // Atualiza a distância em Km
    }

    private fun updateBatimentos(batimentos: Int) {
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
        val handlerPassosDistancia = Handler(mainLooper)
        val handlerBatimentos = Handler(mainLooper)
        var passos = 0
        var batimentos = 70
        var aumentarBatimentos = true

        // Runnable para atualizar passos e distância a cada 2,5 segundos
        val runnablePassosDistancia = object : Runnable {
            override fun run() {
                passos += (5..15).random()  // Simula entre 5 e 15 passos por atualização
                updatePassosEDistancia(passos)
                handlerPassosDistancia.postDelayed(this, 2500)  // Atualiza a cada 2,5 segundos
            }
        }

        // Runnable para atualizar os batimentos cardíacos a cada 1 minuto
        val runnableBatimentos = object : Runnable {
            override fun run() {
                // Simula a variação de batimentos cardíacos
                if (aumentarBatimentos) {
                    batimentos += (1..3).random()  // Aumenta os batimentos
                    if (batimentos >= 100) aumentarBatimentos = false  // Se alcançar 100, começa a diminuir
                } else {
                    batimentos -= (1..3).random()  // Diminui os batimentos
                    if (batimentos <= 60) aumentarBatimentos = true  // Se alcançar 60, começa a aumentar
                }

                updateBatimentos(batimentos)
                handlerBatimentos.postDelayed(this, 60000)  // Atualiza a cada 1 minuto (60.000 milissegundos)
            }
        }

        // Inicia ambos os Runnables
        handlerPassosDistancia.post(runnablePassosDistancia)
        handlerBatimentos.post(runnableBatimentos)
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
