package com.example.divisa_1.worker

import android.content.Context
import android.content.SharedPreferences
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object WorkManagerConfig {

    // Función para configurar la sincronización periódica cada hora
    fun setupHourlySync(context: Context) {
        // Crear una solicitud de trabajo periódica para ejecutar CurrencyWorker cada 1 hora
        val syncRequest = PeriodicWorkRequest.Builder(CurrencyWorker::class.java, 1, TimeUnit.HOURS)
            .build()

        // Encolar el trabajo con WorkManager para que se ejecute automáticamente
        WorkManager.getInstance(context).enqueue(syncRequest)

        // Guardar la hora de la última sincronización en las preferencias compartidas
        saveLastUpdateTime(context)
    }

    // Función para guardar la hora de la última sincronización en las preferencias compartidas
    private fun saveLastUpdateTime(context: Context) {
        // Obtener las preferencias compartidas de la aplicación
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Obtener la hora actual en formato de 24 horas (HH:mm)
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        // Guardar la hora de la última sincronización en las preferencias
        editor.putString("LAST_SYNC_TIME", currentTime)  // 🔹 Guardamos la hora de la última sincronización con la API
        editor.apply()  // Aplicar los cambios realizados en las preferencias
    }
}
