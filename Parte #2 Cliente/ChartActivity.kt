package com.example.divisa_cliente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Color
import androidx.compose.material3.Text

// Actividad que muestra la gráfica de variación del tipo de cambio
class ChartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener los datos enviados desde MainActivity
        val currency = intent.getStringExtra("currency") ?: return
        val startDate = intent.getStringExtra("startDate") ?: return
        val endDate = intent.getStringExtra("endDate") ?: return

        setContent {
            ChartScreen(currency, startDate, endDate) // Llama a la interfaz de usuario de la gráfica
        }
    }
}

// Composable que genera la pantalla de la gráfica
@Composable
fun ChartScreen(currency: String, startDate: String, endDate: String) {
    val context = LocalContext.current

    // Obtener los datos de la tasa de cambio desde DataProvider
    val dataPoints by remember { mutableStateOf(DataProvider.getExchangeRates(context, currency, startDate, endDate)) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (dataPoints.isNotEmpty()) {
            // Consulta adicional para obtener el tipo de cambio del peso mexicano
            val mxnDataPoints = DataProvider.getExchangeRates(context, "MXN", startDate, endDate)
            val latestMxnValue = mxnDataPoints.lastOrNull()?.y ?: 0f // Último valor del peso mexicano

            // Última tasa de cambio obtenida de los datos de la moneda extranjera
            val latestExchangeRate = dataPoints.last().y

            // Mostrar el tipo de cambio más reciente en la parte superior
            Text(
                text = String.format("%.4f MXN = %.4f %s", latestMxnValue, latestExchangeRate, currency),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            // Integración de la vista de la gráfica en Compose
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    LineChart(ctx).apply {
                        // Crear las entradas para la gráfica
                        val entries = dataPoints.mapIndexed { index, dataPoint ->
                            Entry(index.toFloat(), dataPoint.y, dataPoint.data) // Índice y valor de la tasa de cambio
                        }

                        // Configurar el conjunto de datos de la gráfica
                        val dataSet = LineDataSet(entries, "Variación de $currency").apply {
                            color = Color.BLUE // Color de la línea
                            valueTextColor = Color.BLACK // Color del texto de valores
                            lineWidth = 2f // Grosor de la línea
                            setCircleColor(Color.RED) // Color de los puntos de la gráfica
                        }

                        data = LineData(dataSet) // Asignar el conjunto de datos a la gráfica

                        // Configurar la descripción de la gráfica
                        description = Description().apply { text = "Tipo de cambio vs. MXN" }

                        // Configurar el eje X para mostrar las fechas
                        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val index = value.toInt()
                                if (index >= 0 && index < dataPoints.size) {
                                    val dateTimeString = dataPoints[index].data as String
                                    val datePart = dateTimeString.split(" ")[0] // Extraer solo la fecha
                                    return datePart
                                }
                                return ""
                            }
                        }
                        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM // Posición de los valores del eje X
                        xAxis.setDrawGridLines(false) // Ocultar líneas de la cuadrícula en el eje X

                        // Configurar el eje Y para mostrar los valores de la moneda
                        axisRight.isEnabled = false // Deshabilitar el eje derecho
                        axisLeft.setDrawGridLines(true) // Mostrar líneas de la cuadrícula en el eje Y

                        invalidate() // Refrescar la gráfica con los datos actualizados
                    }
                }
            )
        }
    }
}
