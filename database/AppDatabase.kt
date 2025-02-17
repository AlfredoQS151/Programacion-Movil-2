package com.example.divisa_1.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Esta clase define la base de datos Room de la aplicación
@Database(entities = [ExchangeRate::class], version = 1) // Se indica que la base de datos tiene la entidad 'ExchangeRate' y su versión es la 1
abstract class AppDatabase : RoomDatabase() {

    // Función abstracta que nos dará acceso al DAO para realizar operaciones sobre la entidad 'ExchangeRate'
    abstract fun exchangeRateDao(): ExchangeRateDao

    companion object {
        // Instancia de la base de datos (se asegura de que solo exista una instancia a lo largo de la aplicación)
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Método para obtener la instancia de la base de datos
        fun getDatabase(context: Context): AppDatabase {
            // Si ya existe una instancia de la base de datos, la devuelve
            return INSTANCE ?: synchronized(this) {
                // Si no existe, la crea
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, // La clase de la base de datos
                    "exchange_rate_database" // El nombre de la base de datos
                ).build()
                // Asignar la instancia creada a la variable INSTANCE
                INSTANCE = instance
                instance // Devuelve la nueva instancia
            }
        }
    }
}
