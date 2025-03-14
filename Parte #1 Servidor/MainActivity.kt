package com.example.divisa_1

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.divisa_1.database.AppDatabase
import com.example.divisa_1.database.ExchangeRate
import com.example.divisa_1.databinding.ActivityMainBinding
import com.example.divisa_1.worker.CurrencyWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // ViewBinding para acceder a los elementos de la UI sin usar findViewById
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflamos el layout usando ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar si hay datos en la base de datos antes de exigir conexión a Internet
        lifecycleScope.launch(Dispatchers.IO) {
            // Obtener instancia de la base de datos
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.exchangeRateDao()

            // Obtener todas las tasas de cambio almacenadas en la BD
            val rates = dao.getAllRates()

            withContext(Dispatchers.Main) {
                if (rates.isEmpty()) {
                    // Si la base de datos está vacía y no hay conexión a Internet, mostrar alerta
                    if (!isInternetAvailable()) {
                        showNoInternetDialog()
                        return@withContext
                    }
                }

                // Si hay Internet, ejecutar el Worker para actualizar los datos
                if (isInternetAvailable()) {
                    val workRequest = OneTimeWorkRequest.Builder(CurrencyWorker::class.java).build()
                    WorkManager.getInstance(this@MainActivity).enqueue(workRequest)
                }

                // Mostrar la última actualización guardada en SharedPreferences
                loadLastUpdateTime()

                // Mostrar las tasas de cambio en la UI
                showExchangeRates(rates)
            }
        }

        // Configurar el botón de reinicio para reiniciar la app
        binding.buttonRestart.setOnClickListener {
            restartApp()
        }
    }

    // Función para mostrar las tasas de cambio en pantalla
    private fun showExchangeRates(rates: List<ExchangeRate>) {
        if (rates.isEmpty()) {
            // Si no hay datos, mostrar un mensaje indicando que se reinicie la app
            binding.textView.text = "Datos cargados, reinicie la app..."
        } else {
            // Convertir la lista de tasas de cambio en un texto formateado
            val ratesText = rates.joinToString("\n") { "${it.currencyCode}: ${it.rate}" }
            binding.textView.text = ratesText
        }
    }

    // Función para cargar la última hora de actualización guardada
    private fun loadLastUpdateTime() {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val lastEntryTime = sharedPreferences.getString("LAST_ENTRY_TIME", "No disponible")
        val lastUpdateTime = sharedPreferences.getString("LAST_UPDATE_TIME", "No disponible")

        // Mostrar los datos en la interfaz de usuario
        binding.textViewLastEntry.text = "Última entrada: $lastEntryTime"
        binding.textViewLastUpdate.text = "Última actualización: $lastUpdateTime"
    }

    override fun onResume() {
        super.onResume()

        // Guardar la hora actual como la última vez que se abrió la app
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentTime = getCurrentTime()
        sharedPreferences.edit().putString("LAST_ENTRY_TIME", currentTime).apply()
    }

    // Función para obtener la hora actual en formato HH:mm
    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(System.currentTimeMillis())
    }

    // Función para verificar si hay conexión a Internet
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // Función para mostrar un mensaje si no hay conexión a Internet
    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sin conexión a Internet")
            .setMessage("Se requiere conexión a Internet para obtener los datos la primera vez. Conéctese y vuelva a intentarlo.")
            .setCancelable(false)
            .setPositiveButton("Salir") { _, _ ->
                finish() // Cerrar la aplicación
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    // Función para reiniciar completamente la aplicación
    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
            finishAffinity() // Cierra todas las actividades
        }
    }
}
