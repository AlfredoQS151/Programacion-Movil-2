package org.example.multiplataform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import multiplataform.composeapp.generated.resources.Res
import multiplataform.composeapp.generated.resources.fondo

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var currentScreen by remember { mutableStateOf("home") }

        val buttonColor = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF36DC18) // Color verde #36dc18
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen de fondo desde commonMain/resources/fondo.jpg
            Image(
                painter = painterResource(Res.drawable.fondo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            when (currentScreen) {
                "home" -> {
                    Column(
                        modifier = Modifier
                            .safeContentPadding()
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { showContent = !showContent },
                            colors = buttonColor,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Desplegar")
                        }

                        AnimatedVisibility(showContent) {
                            Column(
                                modifier = Modifier.padding(top = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = { currentScreen = "characters" },
                                    colors = buttonColor,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text("Personajes")
                                }

                                Button(
                                    onClick = { currentScreen = "locations" },
                                    colors = buttonColor,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text("Ubicaciones")
                                }

                                Button(
                                    onClick = { currentScreen = "episodes" },
                                    colors = buttonColor,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text("Episodios")
                                }
                            }
                        }
                    }
                }

                "characters" -> CharacterScreen(onBack = { currentScreen = "home" })
                "locations" -> LocationScreen(onBack = { currentScreen = "home" })
                "episodes" -> EpisodeScreen(onBack = { currentScreen = "home" })
            }
        }
    }
}

