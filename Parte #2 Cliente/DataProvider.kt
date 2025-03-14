package com.example.divisa_cliente

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.github.mikephil.charting.data.Entry
import java.text.SimpleDateFormat
import java.util.*

object DataProvider {

    fun getExchangeRates(context: Context, currency: String, startDate: String, endDate: String): List<Entry> {
        val contentUri = Uri.parse("content://com.example.divisa_1.provider.CurrencyProvider/exchange_rates")

        // Log para verificar los parámetros antes de realizar la consulta
        Log.d("DataProvider", "Consultando con moneda: $currency, Desde: $startDate, Hasta: $endDate")

        // Definir el formato de fecha y hora esperado
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Convertir las fechas de entrada a objetos Date
        val startDateTime = formatter.parse(startDate) ?: Date()
        val endDateTime = formatter.parse(endDate) ?: Date()

        // Ajustar la fecha de finalización al último segundo del día si es necesario
        val calendar = Calendar.getInstance()
        calendar.time = endDateTime
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val adjustedEndDate = formatter.format(calendar.time)

        // Log para verificar la fecha de fin ajustada
        Log.d("DataProvider", "Fecha ajustada final: $adjustedEndDate")

        // Realizar la consulta al ContentProvider
        val cursor: Cursor? = context.contentResolver.query(
            contentUri,
            null, // Recuperar todas las columnas
            "currencyCode = ? AND timestamp BETWEEN ? AND ?", // Condición de búsqueda
            arrayOf(currency, startDate, adjustedEndDate), // Argumentos para la consulta
            "timestamp ASC" // Ordenar los resultados por fecha ascendente
        )

        val entries = mutableListOf<Entry>()

        // Verificar si la consulta devolvió resultados
        if (cursor != null && cursor.moveToFirst()) {
            val rateIndex = cursor.getColumnIndex("rate") // Índice de la columna de la tasa de cambio
            val timestampIndex = cursor.getColumnIndex("timestamp") // Índice de la columna de la fecha/hora

            // Validar que las columnas necesarias existen en el resultado
            if (rateIndex == -1 || timestampIndex == -1) {
                Log.e("DataProvider", "La columna 'rate' o 'timestamp' no se encuentra en la consulta.")
                return emptyList() // Retornar lista vacía si las columnas no están disponibles
            }

            var index = 0f // Contador para el eje X de la gráfica
            do {
                val rate = cursor.getDouble(rateIndex) // Obtener la tasa de cambio
                val timestamp = cursor.getString(timestampIndex) // Obtener la fecha/hora

                // Log para verificar cada registro obtenido
                Log.d("DataProvider", "Obtenido: Fecha=$timestamp, Rate=$rate")

                // Agregar entrada a la lista con el índice, el valor de la tasa y la fecha/hora como etiqueta
                entries.add(Entry(index++, rate.toFloat(), timestamp))
            } while (cursor.moveToNext())

            // Log para indicar cuántos datos fueron recuperados
            Log.d("DataProvider", "Total de puntos obtenidos: ${entries.size}")
        } else {
            // Log si no se encontraron resultados
            Log.e("DataProvider", "No se encontraron resultados para los parámetros dados.")
        }

        // Cerrar el cursor para liberar recursos
        cursor?.close()
        return entries
    }
}
