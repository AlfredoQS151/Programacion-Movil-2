package com.example.telefonia

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var etPhoneNumber: EditText
    private lateinit var etAutoReplyMessage: EditText
    private lateinit var btnSave: Button

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach { (permission, granted) ->
                Log.d("MainActivity", "Permiso: $permission, Concedido: $granted")
            }
            if (permissions.values.any { !it }) {
                Toast.makeText(this, "Se requieren permisos para el correcto funcionamiento", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etAutoReplyMessage = findViewById(R.id.etAutoReplyMessage)
        btnSave = findViewById(R.id.btnSave)

        checkAndRequestPermissions()
        loadSavedData()

        btnSave.setOnClickListener {
            saveData()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CALL_LOG // Se añade para obtener el número de llamada entrante
        )

        val notGrantedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGrantedPermissions.isNotEmpty()) {
            Log.d("MainActivity", "Solicitando permisos: ${notGrantedPermissions.joinToString()}")
            requestPermissionsLauncher.launch(notGrantedPermissions.toTypedArray())
        } else {
            Log.d("MainActivity", "Todos los permisos ya están concedidos")
        }
    }

    private fun saveData() {
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val message = etAutoReplyMessage.text.toString().trim()

        if (phoneNumber.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa ambos valores", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        editor.putString("saved_number", phoneNumber)
        editor.putString("auto_reply_message", message)
        editor.apply()

        Log.d("MainActivity", "Datos guardados: Numero=$phoneNumber, Mensaje=$message")
        Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
    }

    private fun loadSavedData() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val savedNumber = prefs.getString("saved_number", "") ?: ""
        val autoReplyMessage = prefs.getString("auto_reply_message", "") ?: ""

        etPhoneNumber.setText(savedNumber)
        etAutoReplyMessage.setText(autoReplyMessage)

        Log.d("MainActivity", "Datos cargados: Numero=$savedNumber, Mensaje=$autoReplyMessage")
    }
}
