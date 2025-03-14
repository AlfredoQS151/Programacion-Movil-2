package com.example.divisa_1.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // La URL base para la API de ExchangeRate-API. Es la dirección principal a la que se le agregan los endpoints.
    private const val BASE_URL = "https://v6.exchangerate-api.com/v6/f31151757733250c01ed12eb/"

    // La propiedad 'instance' es un Singleton que inicializa Retrofit y proporciona
    // una instancia de la interfaz 'CurrencyApi' para realizar las solicitudes a la API.
    val instance: CurrencyApi by lazy {
        // Se construye una instancia de Retrofit utilizando un builder
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Establece la URL base de la API
            .addConverterFactory(GsonConverterFactory.create()) // Utiliza Gson para convertir las respuestas JSON en objetos Kotlin
            .build() // Construye el objeto Retrofit
            .create(CurrencyApi::class.java) // Crea una instancia de la interfaz 'CurrencyApi', que define los métodos para las solicitudes
    }
}