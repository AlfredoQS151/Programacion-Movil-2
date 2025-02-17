package com.example.divisa_1.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Esta clase define la entidad 'ExchangeRate' que representa la tabla 'exchange_rates' en la base de datos
@Entity(tableName = "exchange_rates") // La anotación @Entity indica que esta clase será una entidad de la base de datos
data class ExchangeRate(
    @PrimaryKey val currencyCode: String,  // El código de la moneda (clave primaria), como "EUR", "GBP", "USD", etc.
    val rate: Double // La tasa de cambio de esa moneda en relación con otra (por ejemplo, USD -> EUR)
)
