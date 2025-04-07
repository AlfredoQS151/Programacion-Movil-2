package com.example.regresoacasa

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*

class LocationHelper(private val context: Context) {

    // Cliente para obtener la ubicación usando los servicios fusionados de Google
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    // Método para obtener la ubicación actual del usuario
    fun getCurrentLocation(onResult: (Location?) -> Unit) {
        // Configuración del pedido de ubicación
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Intervalo de 10 segundos entre actualizaciones
            fastestInterval = 5000 // Intervalo más rápido para recibir ubicación (5 segundos)
            priority = Priority.PRIORITY_HIGH_ACCURACY // Prioridad de precisión alta para obtener ubicación
            maxWaitTime = 15000 // Tiempo máximo de espera (15 segundos)
            numUpdates = 1 // Solo obtener una actualización de la ubicación
        }

        // Callback para manejar el resultado de la ubicación
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                onResult(location) // Llamar la función de resultado con la ubicación obtenida
                fusedLocationClient.removeLocationUpdates(this) // Dejar de recibir actualizaciones después de obtener la ubicación
            }
        }

        // Solicitar actualizaciones de ubicación
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper() // Ejecutar en el hilo principal
        )
    }
}
