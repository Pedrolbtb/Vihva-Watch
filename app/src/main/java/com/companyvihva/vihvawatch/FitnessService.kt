package com.companyvihva.vihvawatch.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat.getSystemService

class FitnessService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var totalSteps: Float = 0f
    private var lastStepCount: Float = 0f
    private var heartRate: Float = 0f
    private lateinit var firestore: FirebaseFirestore
    private var userUid: String? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        firestore = FirebaseFirestore.getInstance()

        // Obtém o UID do usuário logado
        userUid = FirebaseAuth.getInstance().currentUser?.uid

        // Registra os sensores necessários para capturar os dados
        registerSensors()
    }

    private fun registerSensors() {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.let { stepCounter ->
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)?.let { heartRateSensor ->
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    if (lastStepCount == 0f) {
                        lastStepCount = event.values[0]
                    }
                    totalSteps = event.values[0] - lastStepCount
                    sendFitnessDataToFirestore("steps", totalSteps)
                }

                Sensor.TYPE_HEART_RATE -> {
                    heartRate = event.values[0]
                    sendFitnessDataToFirestore("heartRate", heartRate)
                }
            }
        }
    }

    private fun sendFitnessDataToFirestore(dataType: String, value: Float) {
        userUid?.let { uid ->
            val userDocRef = firestore.collection("clientes").document(uid)

            // Define um mapa para os dados que serão atualizados
            val fitnessData = mapOf(
                dataType to value,
                "timestamp" to System.currentTimeMillis()
            )

            // Atualiza o documento do usuário com os dados de fitness capturados
            userDocRef.collection("fitness_data").add(fitnessData)
                .addOnSuccessListener {
                    Log.d("FitnessService", "Dados de $dataType enviados para Firestore com sucesso.")
                }
                .addOnFailureListener { e ->
                    Log.w("FitnessService", "Erro ao enviar dados de $dataType para Firestore.", e)
                }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Método requerido pelo SensorEventListener, mas não utilizado no momento.
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove a escuta dos sensores ao destruir o serviço
        sensorManager.unregisterListener(this)
    }
}
