package com.example.firebasecloudmessenging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.firebasecloudmessenging.ui.theme.FirebaseCloudMessengingTheme
import com.google.firebase.messaging.FirebaseMessaging
import androidx.core.app.NotificationCompat

class MainActivity : ComponentActivity() {

    private var deviceToken by mutableStateOf("Obteniendo token...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        createNotificationChannel()

        // Obtener token FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                deviceToken = task.result
                Log.d("FCM", "Token: $deviceToken")
            } else {
                Log.w("FCM", "Error obteniendo token", task.exception)
                deviceToken = "Error al obtener token"
            }
        }

        // Suscribirse a topic
        FirebaseMessaging.getInstance().subscribeToTopic("general")

        setContent {
            FirebaseCloudMessengingTheme {
                val context = LocalContext.current
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "Token del dispositivo:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = deviceToken,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("FCM Token", deviceToken)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Token copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Copiar token")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default Channel"
            val descriptionText = "Canal por defecto para notificaciones FCM"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("default_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
