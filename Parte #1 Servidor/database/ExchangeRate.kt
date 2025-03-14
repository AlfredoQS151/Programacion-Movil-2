package com.example.divisa_1.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Identificador único para cada entrada
    val currencyCode: String,  // Código de la moneda (Ej: USD, EUR)
    val rate: Double,  // Tasa de cambio de la moneda
    val timestamp: String // Nueva columna para almacenar la fecha y hora de la consulta
)
