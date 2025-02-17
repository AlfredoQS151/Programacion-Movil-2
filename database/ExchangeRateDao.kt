package com.example.divisa_1.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// La interfaz @Dao proporciona los métodos de acceso a la base de datos (Data Access Object)
@Dao
interface ExchangeRateDao {

    // Método para insertar una lista de tasas de cambio en la base de datos.
    // Si un registro ya existe (basado en la clave primaria 'currencyCode'), se reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)  // Esto reemplaza los registros existentes si hay un conflicto (por ejemplo, misma clave primaria)
    suspend fun insertRates(rates: List<ExchangeRate>)

    // Método para obtener todas las tasas de cambio de la base de datos.
    // Devuelve una lista de objetos 'ExchangeRate' (moneda y tasa).
    @Query("SELECT * FROM exchange_rates")  // Consulta SQL para seleccionar todos los registros de la tabla 'exchange_rates'
    suspend fun getAllRates(): List<ExchangeRate>  // Retorna una lista de 'ExchangeRate'
}
