package com.example.divisa_1.database

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExchangeRateDao {

    // Insertar datos con la nueva estructura (incluyendo timestamp)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRate>)

    // Obtener todos los registros ordenados por fecha y hora de consulta
    @Query("SELECT * FROM exchange_rates ORDER BY timestamp DESC")
    suspend fun getAllRates(): List<ExchangeRate>

    // Nuevos métodos para el ContentProvider
    @Query("SELECT * FROM exchange_rates ORDER BY timestamp DESC")
    fun getAllRatesCursor(): Cursor

    @Query("SELECT * FROM exchange_rates WHERE id = :id")
    fun getRateByIdCursor(id: Long): Cursor

    @Query("INSERT INTO exchange_rates (currencyCode, rate, timestamp) VALUES (:currencyCode, :rate, :timestamp)")
    suspend fun insertRate(currencyCode: String, rate: Double, timestamp: String)

    @Query("""
        SELECT * FROM exchange_rates 
        WHERE currencyCode = :currencyCode 
        AND timestamp BETWEEN :startDate AND :endDate
        ORDER BY timestamp ASC
    """)
    fun getRatesByCurrencyAndDateRangeCursor(currencyCode: String, startDate: String, endDate: String): Cursor
}
