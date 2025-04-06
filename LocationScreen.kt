package com.example.regresoacasa

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.regresoacasa.map.MapViewComponent

@Composable
fun LocationScreen(location: Location) {
    var direccionCasa by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Mapa
        MapViewComponent(
            latitude = location.latitude,
            longitude = location.longitude,
            destinationAddress = direccionCasa
        )

        // UI superior: ubicación y campo de dirección
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            
            OutlinedTextField(
                value = direccionCasa,
                onValueChange = { direccionCasa = it },
                label = { Text("Dirección de tu casa") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
