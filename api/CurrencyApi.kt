package com.example.divisa_1.api

import retrofit2.Response
import retrofit2.http.GET

// La interfaz 'CurrencyApi' define los endpoints para interactuar con la API externa
interface CurrencyApi {

    // El endpoint 'latest/USD' se usa para obtener las tasas de cambio más recientes con respecto al dólar estadounidense (USD).
    // La anotación @GET indica que esta es una solicitud GET hacia la URL definida por la base URL de Retrofit (https://v6.exchangerate-api.com/v6/f31151757733250c01ed12eb/).
    @GET("latest/USD")  // ✅ No se necesita un parámetro aquí, 'USD' es estático en la URL
    suspend fun getExchangeRates(): Response<CurrencyResponse> // ✅ Sin parámetros adicionales

}
