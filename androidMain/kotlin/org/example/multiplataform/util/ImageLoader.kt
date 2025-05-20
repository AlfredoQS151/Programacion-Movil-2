package org.example.multiplataform.util

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*

actual suspend fun loadImage(url: String): ImageBitmap? {
    return try {
        val client = HttpClient(OkHttp)
        val response: ByteArray = client.get(url).body()
        val bitmap = BitmapFactory.decodeByteArray(response, 0, response.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
