package org.example.multiplataform

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import multiplataform.composeapp.generated.resources.Res
import multiplataform.composeapp.generated.resources.fondo_ubi
import org.example.multiplataform.model.Location
import org.example.multiplataform.network.LocationApi
import org.jetbrains.compose.resources.painterResource

@Composable
fun LocationScreen(onBack: () -> Unit) {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(Res.drawable.fondo_ubi),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "Ubicaciones",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val locationsState = produceState<List<Location>?>(initialValue = null) {
                    value = try {
                        LocationApi.fetchLocations().results
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                locationsState.value?.let { locations ->
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(locations) { location ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Nombre: ${location.name}", style = MaterialTheme.typography.titleMedium)
                                    Text("Tipo: ${location.type}")
                                    Text("Dimensión: ${location.dimension}")
                                }
                            }
                        }
                    }
                } ?: run {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF36DC18)),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Volver al inicio")
                }
            }
        }
    }
}
