package com.example.regresoacasa

import LocationScreen
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    private lateinit var locationHelper: LocationHelper  // Instancia para obtener la ubicación

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationHelper = LocationHelper(this)  // Inicializa la clase para obtener la ubicación

        // Pedimos los permisos de ubicación
        requestLocationPermission()  // Solicita permisos de ubicación
    }

    // Función para solicitar los permisos de ubicación
    private fun requestLocationPermission() {
        // Crea un lanzador de solicitud de permisos
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                // Verifica si el permiso de ubicación fue concedido
                val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                if (granted) {
                    // Si los permisos fueron concedidos, obtiene la ubicación
                    getLocation()
                } else {
                    // Si los permisos no fueron concedidos, muestra un mensaje
                    Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_LONG).show()
                }
            }

        // Verifica si el permiso de ubicación fina ya fue concedido
        val fineLocationGranted = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Si el permiso ya fue concedido, obtiene la ubicación
        if (fineLocationGranted) {
            getLocation()
        } else {
            // Si no, solicita los permisos necesarios
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,  // Permiso de ubicación precisa
                    Manifest.permission.ACCESS_COARSE_LOCATION  // Permiso de ubicación aproximada
                )
            )
        }
    }

    // Función para obtener la ubicación del dispositivo
    private fun getLocation() {
        // Llama al método de LocationHelper para obtener la ubicación
        locationHelper.getCurrentLocation { location: Location? ->
            // Si la ubicación es obtenida correctamente
            if (location != null) {
                // Establece el contenido de la actividad con la pantalla principal y la ubicación obtenida
                setContent {
                    LocationScreen(location)
                }
            } else {
                // Si no se pudo obtener la ubicación, muestra un mensaje de error
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_LONG).show()
            }
        }
    }
}
