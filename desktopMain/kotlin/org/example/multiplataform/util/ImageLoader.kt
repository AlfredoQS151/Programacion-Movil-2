package org.example.multiplataform.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual suspend fun loadImage(url: String): ImageBitmap? {
    return try {
        val client = HttpClient(CIO)
        val bytes: ByteArray = client.get(url).body()
        val inputStream = ByteArrayInputStream(bytes)
        val bufferedImage = ImageIO.read(inputStream)
        bufferedImage?.toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}
