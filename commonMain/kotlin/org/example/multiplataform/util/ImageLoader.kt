package org.example.multiplataform.util

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun loadImage(url: String): ImageBitmap?
