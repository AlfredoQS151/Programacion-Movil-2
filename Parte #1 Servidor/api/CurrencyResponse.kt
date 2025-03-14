package com.example.divisa_1.api

// Esta es una data class que representa la respuesta que se recibe de la API.
// La respuesta contiene la información de las tasas de cambio y detalles sobre la consulta.

data class CurrencyResponse(
    val base: String, // La moneda base con la que se comparan todas las demás monedas (ejemplo: "USD" para dólares estadounidenses)
    val date: String, // La fecha en la que se obtuvieron las tasas de cambio
    val time_last_updated: Long, // El timestamp de la última actualización, representado como un valor largo (en segundos)
    val conversion_rates: Map<String, Double> // Un mapa que contiene los códigos de las monedas como claves (ejemplo: "EUR", "GBP") y las tasas de cambio como valores (ejemplo: 0.84 para EUR)
)
