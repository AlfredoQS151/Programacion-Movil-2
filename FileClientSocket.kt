package com.example.wifidirect

import java.io.BufferedInputStream
import java.io.OutputStream
import java.io.FileInputStream
import java.net.Socket

class FileClientSocket(private val host: String, private val filePath: String) : Thread() {
    override fun run() {
        var socket: Socket? = null
        var fileInputStream: FileInputStream? = null
        var bufferedInputStream: BufferedInputStream? = null
        var outputStream: OutputStream? = null
        try {
            // Conectamos al servidor (el otro dispositivo)
            socket = Socket(host, 8888)

            // Preparamos el archivo
            val file = java.io.File(filePath)
            fileInputStream = FileInputStream(file)
            bufferedInputStream = BufferedInputStream(fileInputStream)
            outputStream = socket.getOutputStream()

            // Leemos el archivo y enviamos en bloques
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bufferedInputStream?.close()
                fileInputStream?.close()
                outputStream?.close()
                socket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
