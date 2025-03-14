package com.example.divisa_1.worker

import android.content.Context
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

        val currencyApi = RetrofitInstance.api

        return try {
            val response: Response<CurrencyResponse> = currencyApi.getExchangeRates()
            Log.d("WorkManagerCheck", "Respuesta de la API recibida: $response")

            if (response.isSuccessful) {
                val exchangeRates = response.body()?.conversion_rates
                Log.d("WorkManagerCheck", "Tasas de cambio recibidas: $exchangeRates")

                if (exchangeRates != null) {
                    saveToDatabase(exchangeRates)
                    Log.d("WorkManagerCheck", "Tasas guardadas en la base de datos.")
                    saveLastUpdateTime()
                } else {
                    Log.e("WorkManagerCheck", "Error: la API devolvió tasas nulas.")
                }

                Result.success()
            } else {
                Log.e("WorkManagerCheck", "Error en la respuesta de la API: ${response.message()}")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e("WorkManagerCheck", "Error en la solicitud HTTP: ${e.message}")
            Result.failure()
        }
    }

    private fun saveToDatabase(rates: Map<String, Double>) {
        val db = AppDatabase.getDatabase(applicationContext)
        val currentTimestamp = getCurrentTimestamp()  // Obtener la fecha y hora actual

        val exchangeRates = rates.map { (currencyCode, rate) ->
            ExchangeRate(currencyCode = currencyCode, rate = rate, timestamp = currentTimestamp)
        }

        CoroutineScope(Dispatchers.IO).launch {
            db.exchangeRateDao().insertRates(exchangeRates)
            Log.d("WorkManagerCheck", "Datos insertados en la base de datos con timestamp: $currentTimestamp")
        }
    }

    private fun saveLastUpdateTime() {
        val sharedPreferences = applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentTime = getCurrentTimestamp()
        sharedPreferences.edit().putString("LAST_UPDATE_TIME", currentTime).apply()
    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())  // Obtener la fecha y hora actual
    }
}
