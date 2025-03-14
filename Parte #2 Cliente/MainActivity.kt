package com.example.divisa_cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen() // Establece la interfaz de usuario con la pantalla principal
        }
    }
}

@Composable
fun MainScreen() {
    var currency by remember { mutableStateOf("") } // Estado para la moneda ingresada
    var startDate by remember { mutableStateOf("") } // Estado para la fecha de inicio ingresada
    var endDate by remember { mutableStateOf("") } // Estado para la fecha de fin ingresada

    val context = LocalContext.current // Obtiene el contexto actual

    Column(modifier = Modifier.padding(16.dp)) {
        // Campo de entrada para la moneda
        TextField(
            value = currency,
            onValueChange = { currency = it },
            label = { Text("Moneda (ej. USD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Campo de entrada para la fecha de inicio
        TextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Fecha de inicio (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Campo de entrada para la fecha de fin
        TextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("Fecha de fin (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para consultar la tasa de cambio
        Button(
            onClick = {
                if (currency.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()) {
                    // Convertir las fechas al formato con hora (yyyy-MM-dd HH:mm:ss)
                    val formattedStartDate = convertToFullDateFormat(startDate)
                    val formattedEndDate = convertToFullDateFormat(endDate)

                    // Crear intent para abrir la actividad de la gráfica con los datos ingresados
                    val intent = Intent(context, ChartActivity::class.java).apply {
                        putExtra("currency", currency)
                        putExtra("startDate", formattedStartDate)
                        putExtra("endDate", formattedEndDate)
                    }
                    // Iniciar la actividad de la gráfica
                    context.startActivity(intent)
                } else {
                    // Mostrar mensaje si algún campo está vacío
                    Toast.makeText(context, "Por favor complete todos los campos.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Obtener Tasa de Cambio") // Texto del botón
        }
    }
}

// Función para convertir una fecha (yyyy-MM-dd) al formato completo con hora (yyyy-MM-dd HH:mm:ss)
fun convertToFullDateFormat(date: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Formato de entrada
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // Formato de salida
        val parsedDate = inputFormat.parse(date) // Convertir la fecha
        outputFormat.format(parsedDate!!) // Formatear y devolver la fecha con hora
    } catch (e: Exception) {
        date // Si hay error, devolver la fecha sin cambios
    }
}

// Vista previa de la pantalla en el editor de Compose
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScreen()
}
