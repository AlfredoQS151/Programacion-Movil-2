import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.location.Location
import com.example.regresoacasa.MapViewComponent

// Pantalla principal de ubicación
@Composable
fun LocationScreen(location: Location) {
    // Definir variables de estado para los campos de texto y la ruta
    var calle by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var direccionCasa by remember { mutableStateOf("") }
    var triggerRoute by remember { mutableStateOf(false) }

    // Diseño de la pantalla con una columna principal
    Column(modifier = Modifier.fillMaxSize()) {
        // Mapa que ocupa el espacio superior, ajustándose al tamaño disponible
        MapViewComponent(
            latitude = location.latitude,
            longitude = location.longitude,
            destinationAddress = direccionCasa,
            triggerRoute = triggerRoute,
            onRouteDrawn = { triggerRoute = false },
            modifier = Modifier
                .weight(1f) // El mapa ocupa el 80% de la pantalla
        )

        // Caja de texto para dirección, en la parte inferior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)  // Fondo blanco para las cajas de texto
                .padding(16.dp)  // Espaciado de 16dp
        ) {
            // Caja de texto para la calle
            OutlinedTextField(
                value = calle,
                onValueChange = { calle = it },  // Actualiza la variable 'calle'
                label = { Text("Calle") },  // Etiqueta de la caja de texto
                singleLine = true,  // Solo una línea de texto
                modifier = Modifier.fillMaxWidth()  // Ocupa todo el ancho disponible
            )
            Spacer(modifier = Modifier.height(8.dp))  // Espacio entre las cajas de texto

            // Caja de texto para el número
            OutlinedTextField(
                value = numero,
                onValueChange = { numero = it },  // Actualiza la variable 'numero'
                label = { Text("Número") },  // Etiqueta de la caja de texto
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))  // Espacio entre las cajas de texto

            // Caja de texto para la ciudad
            OutlinedTextField(
                value = ciudad,
                onValueChange = { ciudad = it },  // Actualiza la variable 'ciudad'
                label = { Text("Ciudad") },  // Etiqueta de la caja de texto
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))  // Espacio entre las cajas de texto y los botones

            // Fila con los dos botones (Trazar Ruta y Borrar Datos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),  // Espaciado en los extremos
                horizontalArrangement = Arrangement.spacedBy(16.dp),  // Espaciado entre botones
                verticalAlignment = Alignment.CenterVertically  // Alineación vertical de los botones
            ) {
                // Botón Trazar Ruta
                Button(
                    onClick = {
                        // Construir dirección concatenando los campos de texto
                        direccionCasa = buildString {
                            if (calle.isNotBlank()) append("$calle ")
                            if (numero.isNotBlank()) append("$numero ")
                            if (ciudad.isNotBlank()) append(ciudad)
                        }

                        if (direccionCasa.isNotBlank()) {  // Si la dirección no está vacía
                            triggerRoute = true  // Activar la traza de la ruta
                        }
                    },
                    modifier = Modifier.weight(1f)  // El botón ocupa el mismo espacio que el otro
                ) {
                    Text("Trazar Ruta")  // Texto en el botón
                }

                // Botón Borrar Datos
                Button(
                    onClick = {
                        // Limpiar las cajas de texto y detener la ruta
                        calle = ""
                        numero = ""
                        ciudad = ""
                        direccionCasa = ""
                        triggerRoute = false  // Detener la ruta si estaba activada
                    },
                    modifier = Modifier.weight(1f)  // El botón ocupa el mismo espacio que el otro
                ) {
                    Text("Borrar Datos")  // Texto en el botón
                }
            }
        }
    }
}
