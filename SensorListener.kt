package com.example.futbolito

import android.hardware.SensorEventListener
import android.hardware.SensorEvent
import android.content.Context
import androidx.compose.ui.geometry.Offset
import android.hardware.Sensor

// Clase que maneja la lógica del movimiento de la pelota utilizando los sensores del dispositivo
class SensorListener(
    val context: Context, // Contexto de la aplicación
    private val ballPosition: Offset, // Posición actual de la pelota en el campo
    private val velocity: Offset, // Velocidad actual de la pelota
    private val screenSize: Offset, // Tamaño de la pantalla (dimensiones del campo)
    private val goalLeft: Float, // Límite izquierdo de la portería
    private val goalRight: Float, // Límite derecho de la portería
    private val updateBallPosition: (Offset, Offset) -> Unit // Función de callback para actualizar la posición y velocidad de la pelota
) {
    // Método para crear un sensorListener que manejará los cambios del acelerómetro
    fun createSensorListener(): SensorEventListener {
        return object : SensorEventListener {
            // Método que se llama cuando hay un cambio en los datos del sensor
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // Obtener los valores del sensor en los ejes X e Y
                    val x = event.values[0]
                    val y = event.values[1]

                    // Actualizar la velocidad de la pelota en función de los cambios del sensor
                    var velocity = this@SensorListener.velocity
                    velocity = Offset(velocity.x - x * 1.5f, velocity.y + y * 1.5f)

                    // Calcular la nueva posición de la pelota sumando la velocidad a la posición actual
                    var newX = ballPosition.x + velocity.x
                    var newY = ballPosition.y + velocity.y

                    // Verificar si la pelota toca los bordes izquierdo o derecho de la pantalla
                    newX = when {
                        newX <= 30f -> { // Si toca el borde izquierdo
                            velocity = velocity.copy(x = -velocity.x * 0.8f) // Invertir la velocidad en X
                            30f // Posicionar la pelota en el borde izquierdo
                        }
                        newX >= screenSize.x - 30f -> { // Si toca el borde derecho
                            velocity = velocity.copy(x = -velocity.x * 0.8f) // Invertir la velocidad en X
                            screenSize.x - 30f // Posicionar la pelota en el borde derecho
                        }
                        else -> newX // Si no toca los bordes, mantener la posición calculada
                    }

                    var newPosition = Offset(newX, newY) // Nueva posición de la pelota
                    var newVelocity = velocity // Nueva velocidad de la pelota

                    // Verificar si la pelota entra en la portería
                    if (newPosition.y <= 50f && newPosition.x in goalLeft..goalRight) { // Gol en la parte superior
                        newPosition = Offset(screenSize.x / 2, screenSize.y / 2) // Reposicionar la pelota en el centro
                        newVelocity = Offset(0f, 0f) // Detener la pelota (sin velocidad)
                    } else if (newPosition.y >= screenSize.y - 50f && newPosition.x in goalLeft..goalRight) { // Gol en la parte inferior
                        newPosition = Offset(screenSize.x / 2, screenSize.y / 2) // Reposicionar la pelota en el centro
                        newVelocity = Offset(0f, 0f) // Detener la pelota (sin velocidad)
                    } else {
                        // Verificar si la pelota toca los bordes superior o inferior de la pantalla
                        if (newPosition.y <= 30f) { // Si toca el borde superior
                            newVelocity = newVelocity.copy(y = -newVelocity.y * 0.8f) // Invertir la velocidad en Y
                            newPosition = newPosition.copy(y = 30f) // Posicionar la pelota en el borde superior
                        }
                        if (newPosition.y >= screenSize.y - 30f) { // Si toca el borde inferior
                            newVelocity = newVelocity.copy(y = -newVelocity.y * 0.8f) // Invertir la velocidad en Y
                            newPosition = newPosition.copy(y = screenSize.y - 30f) // Posicionar la pelota en el borde inferior
                        }
                    }

                    // Llamar a la función de actualización de la posición y velocidad de la pelota
                    updateBallPosition(newPosition, newVelocity)
                }
            }

            // Método que se llama cuando la precisión del sensor cambia (no se usa aquí)
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }
}
