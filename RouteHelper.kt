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

    private const val API_KEY = "5b3ce3597851110001cf62482dba78307b1e4d6bbbf4ee656997ee0c"

    suspend fun geocodeAddress(address: String): Pair<Double, Double>? {
        val url = "https://api.openrouteservice.org/geocode/search?api_key=$API_KEY&text=${address}"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val json = JSONObject(response.body?.string() ?: return@withContext null)
                val features = json.getJSONArray("features")
                if (features.length() == 0) return@withContext null
                val coordinates = features.getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")
                val lon = coordinates.getDouble(0)
                val lat = coordinates.getDouble(1)
                Pair(lat, lon)
            } catch (e: Exception) {
                Log.e("RouteHelper", "Geocode error: ${e.message}")
                null
            }
        }
    }

    suspend fun getRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): List<GeoPoint>? {
        val url = "https://api.openrouteservice.org/v2/directions/foot-walking/geojson"
        val client = OkHttpClient()

        val jsonBody = """
            {
              "coordinates":[[$startLon, $startLat], [$endLon, $endLat]]
            }
        """.trimIndent()

        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", API_KEY)
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val json = JSONObject(response.body?.string() ?: return@withContext null)
                val coords = json.getJSONArray("features")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")

                val routePoints = mutableListOf<GeoPoint>()
                for (i in 0 until coords.length()) {
                    val coord = coords.getJSONArray(i)
                    val lon = coord.getDouble(0)
                    val lat = coord.getDouble(1)
                    routePoints.add(GeoPoint(lat, lon))
                }
                routePoints
            } catch (e: Exception) {
                Log.e("RouteHelper", "Route error: ${e.message}")
                null
            }
        }
    }
}
