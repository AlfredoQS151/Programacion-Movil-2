package com.example.regresoacasa

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.osmdroid.util.GeoPoint

object RouteHelper {

    // API key para acceder a la API de OpenRouteService.
    private const val API_KEY = "5b3ce3597851110001cf62482dba78307b1e4d6bbbf4ee656997ee0c"

    // Función para geocodificar una dirección a coordenadas (latitud y longitud).
    suspend fun geocodeAddress(address: String): Pair<Double, Double>? {
        // URL de la API de geocodificación con la dirección pasada como parámetro.
        val url = "https://api.openrouteservice.org/geocode/search?api_key=$API_KEY&text=${address}"

        // Creamos un cliente HTTP para realizar la solicitud.
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        // Ejecutamos la solicitud en un hilo de IO para no bloquear el hilo principal.
        return withContext(Dispatchers.IO) {
            try {
                // Realizamos la llamada a la API.
                val response = client.newCall(request).execute()
                // Convertimos la respuesta en un objeto JSON.
                val json = JSONObject(response.body?.string() ?: return@withContext null)
                // Extraemos el arreglo "features" que contiene los resultados de la geocodificación.
                val features = json.getJSONArray("features")
                // Si no hay resultados, devolvemos null.
                if (features.length() == 0) return@withContext null
                // Extraemos las coordenadas (longitud, latitud) del primer resultado.
                val coordinates = features.getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")
                // Extraemos la longitud y latitud.
                val lon = coordinates.getDouble(0)
                val lat = coordinates.getDouble(1)
                // Devolvemos las coordenadas como un par de valores (latitud, longitud).
                Pair(lat, lon)
            } catch (e: Exception) {
                // En caso de error, lo registramos en los logs y devolvemos null.
                Log.e("RouteHelper", "Geocode error: ${e.message}")
                null
            }
        }
    }

    // Función para obtener una ruta entre dos puntos (latitud, longitud) usando la API de direcciones.
    suspend fun getRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): List<GeoPoint>? {
        // URL de la API para obtener direcciones
        val url = "https://api.openrouteservice.org/v2/directions/foot-walking/geojson"
        val client = OkHttpClient()

        // Construimos el cuerpo JSON para la solicitud POST, que incluye las coordenadas de inicio y fin.
        val jsonBody = """
            {
              "coordinates":[[$startLon, $startLat], [$endLon, $endLat]]
            }
        """.trimIndent()

        // Convertimos el cuerpo JSON a un tipo adecuado para la solicitud.
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        // Creamos la solicitud POST con la URL, cuerpo y encabezados (autorización con la API Key).
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", API_KEY)
            .post(body)
            .build()

        // Ejecutamos la solicitud en un hilo de IO.
        return withContext(Dispatchers.IO) {
            try {
                // Realizamos la llamada a la API.
                val response = client.newCall(request).execute()
                // Convertimos la respuesta en un objeto JSON.
                val json = JSONObject(response.body?.string() ?: return@withContext null)
                // Extraemos las coordenadas de la ruta (camino calculado).
                val coords = json.getJSONArray("features")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")

                // Creamos una lista para almacenar los puntos de la ruta (como GeoPoint).
                val routePoints = mutableListOf<GeoPoint>()
                // Iteramos a través de las coordenadas de la ruta.
                for (i in 0 until coords.length()) {
                    val coord = coords.getJSONArray(i)
                    val lon = coord.getDouble(0)
                    val lat = coord.getDouble(1)
                    // Agregamos cada coordenada a la lista de puntos de la ruta.
                    routePoints.add(GeoPoint(lat, lon))
                }
                // Devolvemos la lista de puntos de la ruta.
                routePoints
            } catch (e: Exception) {
                // En caso de error, lo registramos en los logs y devolvemos null.
                Log.e("RouteHelper", "Route error: ${e.message}")
                null
            }
        }
    }
}
