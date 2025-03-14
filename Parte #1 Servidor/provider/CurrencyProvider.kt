package com.example.divisa_1.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.divisa_1.database.AppDatabase

class CurrencyProvider : ContentProvider() {

    // Instancia de la base de datos
    private lateinit var database: AppDatabase

    companion object {
        // Autoridad del ContentProvider
        const val AUTHORITY = "com.example.divisa_1.provider.CurrencyProvider"

        // Nombre de la tabla que se expondrá a través del ContentProvider
        const val EXCHANGE_RATES_TABLE = "exchange_rates"

        // URI base para acceder a los datos
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$EXCHANGE_RATES_TABLE")

        // Códigos de URI para la tabla completa y registros individuales
        const val EXCHANGE_RATES = 1
        const val EXCHANGE_RATES_ID = 2

        // UriMatcher para identificar los diferentes tipos de consultas
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, EXCHANGE_RATES_TABLE, EXCHANGE_RATES) // Consulta de todos los registros
            addURI(AUTHORITY, "$EXCHANGE_RATES_TABLE/#", EXCHANGE_RATES_ID) // Consulta de un registro por ID
        }
    }

    override fun onCreate(): Boolean {
        // Inicializa la base de datos cuando el ContentProvider es creado
        context?.let {
            database = AppDatabase.getDatabase(it)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        // Determina el tipo de consulta a partir de la URI
        val code = uriMatcher.match(uri)
        return when (code) {
            EXCHANGE_RATES -> {
                // Si se proporcionan parámetros, filtra por código de moneda y rango de fechas
                if (selectionArgs != null && selectionArgs.size == 3) {
                    val currencyCode = selectionArgs[0]
                    val startDate = selectionArgs[1]
                    val endDate = selectionArgs[2]
                    database.exchangeRateDao().getRatesByCurrencyAndDateRangeCursor(currencyCode, startDate, endDate)
                } else {
                    // Si no se especifican parámetros, devuelve todos los registros
                    database.exchangeRateDao().getAllRatesCursor()
                }
            }
            EXCHANGE_RATES_ID -> {
                // Consulta un solo registro por su ID
                val id = ContentUris.parseId(uri)
                database.exchangeRateDao().getRateByIdCursor(id)
            }
            else -> {
                throw IllegalArgumentException("Unknown URI: $uri") // URI desconocida
            }
        }
    }

    override fun getType(uri: Uri): String? {
        // Devuelve el tipo de MIME según el tipo de consulta
        return when (uriMatcher.match(uri)) {
            EXCHANGE_RATES -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$EXCHANGE_RATES_TABLE" // Para múltiples registros
            EXCHANGE_RATES_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.$EXCHANGE_RATES_TABLE" // Para un solo registro
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Operación de inserción no permitida en este ContentProvider
        throw UnsupportedOperationException("Insert operation is not supported")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        // Operación de eliminación no permitida en este ContentProvider
        throw UnsupportedOperationException("Delete operation is not supported")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        // Operación de actualización no permitida en este ContentProvider
        throw UnsupportedOperationException("Update operation is not supported")
    }
}