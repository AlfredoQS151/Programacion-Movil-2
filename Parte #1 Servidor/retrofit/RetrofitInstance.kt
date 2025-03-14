package com.example.divisa_1.retrofit

import com.example.divisa_1.api.CurrencyApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Creamos una instancia de Retrofit para interactuar con la API
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://v6.exchangerate-api.com/v6/f31151757733250c01ed12eb/")  // La URL base de la API
            .addConverterFactory(GsonConverterFactory.create())  // Convierte el JSON a objetos Kotlin
            .build()  // Construye la instancia de Retrofit
    }

    // Método para obtener una instancia de CurrencyApi
    val api: CurrencyApi by lazy {
        retrofit.create(CurrencyApi::class.java)  // Crea una instancia de la interfaz CurrencyApi para interactuar con la API
    }
}
