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
import com.example.divisa_1.databinding.ActivityMainBinding
import com.example.divisa_1.worker.CurrencyWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // ViewBinding para acceder a los elementos de la UI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // Inflamos el layout
        setContentView(binding.root) // Establecemos el layout como vista principal

        // Verificamos si hay conexión a Internet antes de continuar
        if (!isInternetAvailable()) {
            showNoInternetDialog() // Si no hay Internet, mostramos un mensaje y detenemos la ejecución
            return
        }

        // Creamos y ejecutamos un Worker para actualizar las tasas de cambio
        val workRequest = OneTimeWorkRequest.Builder(CurrencyWorker::class.java).build()
        WorkManager.getInstance(this).enqueue(workRequest)

        // Cargamos y mostramos la última entrada y actualización de la app
        loadLastUpdateTime()

        // Obtenemos las preferencias compartidas para verificar si los datos han sido cargados
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val dataLoaded = sharedPreferences.getBoolean("DATA_LOADED", false)

        if (!dataLoaded) {
            // Si los datos no han sido cargados antes, mostramos un mensaje y un botón de reinicio
            binding.textViewMessage.visibility = android.view.View.VISIBLE
            binding.buttonRestart.visibility = android.view.View.VISIBLE
        }

        // Cargamos las tasas de cambio desde la base de datos en un hilo secundario
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext) // Obtenemos la instancia de la BD
            val dao = db.exchangeRateDao() // Obtenemos el DAO de tasas de cambio
            val rates = dao.getAllRates() // Obtenemos todas las tasas almacenadas en la BD

            withContext(Dispatchers.Main) { // Volvemos al hilo principal para actualizar la UI
                if (rates.isEmpty()) {
                    binding.textView.text = "Esperando datos..." // Si no hay datos, mostramos un mensaje
                } else {
                    // Convertimos la lista de tasas en texto y la mostramos en la UI
                    val ratesText = rates.joinToString("\n") { "${it.currencyCode}: ${it.rate}" }
                    binding.textView.text = ratesText

                    // Marcamos que los datos han sido cargados correctamente
                    sharedPreferences.edit().putBoolean("DATA_LOADED", true).apply()

                    // Ocultamos el mensaje y el botón de reinicio
                    binding.textViewMessage.visibility = android.view.View.GONE
                    binding.buttonRestart.visibility = android.view.View.GONE
                }
            }
        }

        // Configuramos el botón de reinicio para reiniciar la aplicación
        binding.buttonRestart.setOnClickListener {
            restartApp()
        }
    }

    // Función para cargar la última hora de entrada y actualización
    private fun loadLastUpdateTime() {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val lastEntryTime = sharedPreferences.getString("LAST_ENTRY_TIME", "No disponible")
        val lastUpdateTime = sharedPreferences.getString("LAST_UPDATE_TIME", "No disponible")

        // Mostramos la información en la UI
        binding.textViewLastEntry.text = "Última entrada: $lastEntryTime"
        binding.textViewLastUpdate.text = "Última actualización: $lastUpdateTime"
    }

    // Se ejecuta cuando el usuario vuelve a la aplicación
    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentTime = getCurrentTime() // Obtenemos la hora actual
        sharedPreferences.edit().putString("LAST_ENTRY_TIME", currentTime).apply() // Guardamos la hora de la entrada
    }

    // Función para obtener la hora actual en formato HH:mm
    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(System.currentTimeMillis())
    }

    // Función para reiniciar la aplicación
    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName) // Obtenemos el intent para relanzar la app
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Limpiamos el stack de actividades
        startActivity(intent) // Iniciamos la actividad principal
    }

    // Función para verificar si hay conexión a Internet
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // Función para mostrar un diálogo si no hay conexión a Internet
    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sin conexión a Internet")
            .setMessage("Se requiere conexión a Internet para obtener los datos. Por favor, conéctese y vuelva a intentarlo.")
            .setCancelable(false) // Evita que el usuario cierre el diálogo sin presionar el botón
            .setPositiveButton("Salir") { _, _ ->
                finish() // Cierra la aplicación
            }
        val alertDialog = builder.create()
        alertDialog.show() // Mostramos el diálogo
    }
}
