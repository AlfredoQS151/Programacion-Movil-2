package com.example.divisa_1.item

// Esta clase representa un objeto que contiene la información de una tasa de cambio
data class ExchangeRateItem(
    val currencyCode: String, // El código de la moneda (por ejemplo, "USD", "EUR", etc.)
    val rate: Double // La tasa de cambio correspondiente a esa moneda
)
