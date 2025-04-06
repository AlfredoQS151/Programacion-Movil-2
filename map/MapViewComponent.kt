package com.example.regresoacasa.map

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.regresoacasa.RouteHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapViewComponent(
    latitude: Double,
    longitude: Double,
    destinationAddress: String = "Tu dirección configurable aquí",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(destinationAddress) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val destination = RouteHelper.geocodeAddress(destinationAddress)
                if (destination != null) {
                    val routePoints = RouteHelper.getRoute(
                        startLat = latitude,
                        startLon = longitude,
                        endLat = destination.first,
                        endLon = destination.second
                    )

                    mapView.overlays.removeIf { it is Polyline }
                    if (!routePoints.isNullOrEmpty()) {
                        val polyline = Polyline().apply {
                            setPoints(routePoints)
                            outlinePaint.color = android.graphics.Color.BLUE
                            outlinePaint.strokeWidth = 6f
                        }
                        mapView.overlays.add(polyline)
                    }

                    // Agregar marcador en destino
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(destination.first, destination.second)
                        title = "Casa"
                    }
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            } catch (e: Exception) {
                Log.e("MapViewComponent", "Error al trazar la ruta: ${e.message}")
            }
        }
    }

    // Mostrar el mapa
    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) {
        val startPoint = GeoPoint(latitude, longitude)
        it.controller.setZoom(15.0)
        it.controller.setCenter(startPoint)

        // Limpiar marcadores antiguos si los hay
        it.overlays.removeIf { overlay -> overlay is Marker }

        // Marcador ubicación actual
        val startMarker = Marker(it).apply {
            position = startPoint
            title = "Mi Ubicación"
        }
        it.overlays.add(startMarker)
    }
}

// Reutilizar MapView para evitar recrearlo
@Composable
private fun rememberMapViewWithLifecycle(context: Context): MapView {
    val ctx = LocalContext.current
    return remember {
        Configuration.getInstance().load(
            ctx,
            ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        MapView(ctx).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true)
            setTilesScaledToDpi(true)
        }
    }
}
