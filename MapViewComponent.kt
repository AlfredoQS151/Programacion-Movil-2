package com.example.regresoacasa

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapViewComponent(
    latitude: Double,  // Latitud actual del dispositivo
    longitude: Double,  // Longitud actual del dispositivo
    destinationAddress: String,  // Dirección de destino
    triggerRoute: Boolean,  // Bandera para activar el trazado de la ruta
    onRouteDrawn: () -> Unit,  // Callback que se ejecuta una vez que la ruta es trazada
    modifier: Modifier = Modifier  // Modificador para aplicar al componente
) {
    val context = LocalContext.current  // Obtiene el contexto actual de la aplicación
    val mapView = rememberMapViewWithLifecycle(context)  // Inicializa el mapa con ciclo de vida de Compose
    val coroutineScope = rememberCoroutineScope()  // Inicializa el scope para corutinas
    var startMarker by remember { mutableStateOf<Marker?>(null) }  // Marker para la ubicación actual

    // Este bloque se ejecuta cada vez que `triggerRoute` cambie
    LaunchedEffect(triggerRoute) {
        if (!triggerRoute || destinationAddress.isBlank()) return@LaunchedEffect  // Si no hay ruta que trazar, no hace nada

        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Geocodifica la dirección de destino a coordenadas
                val destination = RouteHelper.geocodeAddress(destinationAddress)
                if (destination != null) {
                    // Obtiene los puntos de la ruta entre la ubicación actual y el destino
                    val routePoints = RouteHelper.getRoute(
                        startLat = latitude,
                        startLon = longitude,
                        endLat = destination.first,
                        endLon = destination.second
                    )

                    // Si la ruta se generó correctamente
                    if (!routePoints.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            // Limpiar los overlays existentes (excepto el marcador de ubicación actual)
                            mapView.overlays.removeIf {
                                it is Polyline || (it is Marker && it.title != "Mi Ubicación")
                            }

                            // Dibuja la ruta en el mapa (Polyline)
                            val polyline = Polyline().apply {
                                setPoints(routePoints)  // Establece los puntos de la ruta
                                outlinePaint.color = android.graphics.Color.BLUE  // Color de la línea de la ruta
                                outlinePaint.strokeWidth = 6f  // Ancho de la línea
                            }
                            mapView.overlays.add(polyline)  // Agrega la ruta al mapa

                            // Agrega un marcador en el destino
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(destination.first, destination.second)  // Posición del destino
                                title = "Casa"  // Título del marcador
                            }
                            mapView.overlays.add(marker)  // Agrega el marcador al mapa

                            // Centra el mapa para mostrar toda la ruta
                            val geoPoints = listOf(
                                GeoPoint(latitude, longitude),  // Punto de inicio
                                GeoPoint(destination.first, destination.second)  // Punto de destino
                            )
                            val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPointsSafe(geoPoints)
                            mapView.zoomToBoundingBox(boundingBox, true)  // Enfoca el mapa en la ruta

                            mapView.invalidate()  // Redibuja el mapa
                            onRouteDrawn()  // Llama al callback para indicar que la ruta se trazó
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MapViewComponent", "Error al trazar la ruta: ${e.message}")  // Si ocurre un error, se muestra en log
            }
        }
    }

    // AndroidView se usa para integrar el MapView tradicional de osmdroid dentro de Compose
    AndroidView(
        factory = { mapView },  // Crea y configura el MapView
        modifier = modifier  // Aplica el modificador al componente
    ) {
        if (startMarker == null) {
            // Si aún no hay marcador, agrega uno en la ubicación actual
            val startPoint = GeoPoint(latitude, longitude)
            val marker = Marker(it).apply {
                position = startPoint  // Establece la ubicación del marcador
                title = "Mi Ubicación"  // Título del marcador
            }
            it.overlays.add(marker)  // Agrega el marcador al mapa
            startMarker = marker  // Guarda la referencia al marcador

            // Configura el zoom y el centro del mapa
            it.controller.setZoom(15.0)
            it.controller.setCenter(startPoint)
        }
    }
}

// Función para recordar la configuración y el ciclo de vida del MapView
@Composable
private fun rememberMapViewWithLifecycle(context: Context): MapView {
    val ctx = LocalContext.current
    return remember {
        Configuration.getInstance().load(
            ctx,
            ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        MapView(ctx).apply {
            setTileSource(TileSourceFactory.MAPNIK)  // Establece la fuente de mosaicos
            setMultiTouchControls(true)  // Habilita los controles de multitáctil
            setBuiltInZoomControls(true)  // Habilita los controles de zoom integrados
            setTilesScaledToDpi(true)  // Ajusta los mosaicos a la densidad de píxeles del dispositivo
        }
    }
}
