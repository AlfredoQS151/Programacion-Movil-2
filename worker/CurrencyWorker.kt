package com.example.divisa_1.worker

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.divisa_1.api.CurrencyResponse
import com.example.divisa_1.database.AppDatabase
import com.example.divisa_1.database.ExchangeRate
import com.example.divisa_1.retrofit.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CurrencyWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("WorkManagerCheck", "El Worker se está ejecutando...")

        val currencyApi = RetrofitInstance.api  // Obtener la instancia de la API de Retrofit

        return try {
            // Realizar la solicitud HTTP a la API para obtener las tasas de cambio
            val response: Response<CurrencyResponse> = currencyApi.getExchangeRates() // ✅ Sin parámetros
            Log.d("WorkManagerCheck", "Respuesta de la API recibida: $response")

            // Verificar si la respuesta de la API fue exitosa
            if (response.isSuccessful) {
                val exchangeRates = response.body()?.conversion_rates  // Obtener las tasas de cambio de la respuesta
                Log.d("WorkManagerCheck", "Tasas de cambio recibidas: $exchangeRates")

                if (exchangeRates != null) {
                    // Guardar las tasas de cambio en la base de datos si son válidas
                    saveToDatabase(exchangeRates)
                    Log.d("WorkManagerCheck", "Tasas guardadas en la base de datos.")

                    // Guardar la hora de la última actualización
                    saveLastUpdateTime()
                } else {
                    Log.e("WorkManagerCheck", "Error: la API devolvió tasas nulas.")
                }

                Result.success()  // Indicar que el trabajo se completó exitosamente
            } else {
                Log.e("WorkManagerCheck", "Error en la respuesta de la API: ${response.message()}")
                Result.failure()  // Indicar que hubo un error en la respuesta de la API
            }
        } catch (e: Exception) {
            // Si ocurrió algún error durante la solicitud HTTP, registrar el error
            Log.e("WorkManagerCheck", "Error en la solicitud HTTP: ${e.message}")
            Result.failure()  // Indicar que hubo un error en la ejecución del trabajo
        }
    }

    private fun saveToDatabase(rates: Map<String, Double>) {
        val db = AppDatabase.getDatabase(applicationContext)  // Obtener la instancia de la base de datos
        val exchangeRates = rates.map { (currencyCode, rate) -> ExchangeRate(currencyCode, rate) }  // Mapear las tasas a objetos ExchangeRate

        // Usar un CoroutineScope para realizar la inserción en la base de datos en un hilo de trabajo
        CoroutineScope(Dispatchers.IO).launch {
            db.exchangeRateDao().insertRates(exchangeRates)  // Insertar las tasas en la base de datos
            Log.d("WorkManagerCheck", "Datos insertados en la base de datos.")
        }
    }

    private fun saveLastUpdateTime() {
        val sharedPreferences = applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)  // Obtener las preferencias compartidas
        val currentTime = getCurrentTimeWithoutMinutes()  // Obtener la hora actual sin minutos
        sharedPreferences.edit().putString("LAST_UPDATE_TIME", currentTime).apply()  // Guardar la hora de la última actualización en las preferencias
    }

    private fun getCurrentTimeWithoutMinutes(): String {
        val calendar = Calendar.getInstance()  // Crear una instancia de Calendar para obtener la hora actual
        calendar.set(Calendar.MINUTE, 0)  // Establecer los minutos a 0
        calendar.set(Calendar.SECOND, 0)  // Establecer los segundos a 0
        calendar.set(Calendar.MILLISECOND, 0)  // Establecer los milisegundos a 0

        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())  // Definir el formato de hora en 24 horas
        return dateFormat.format(calendar.time)  // Formatear y devolver la hora actual con minutos a 00
    }

}
