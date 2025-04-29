package com.example.wifidirect

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter

    private lateinit var buttonSearch: Button
    private lateinit var buttonSendImage: Button
    private lateinit var listView: ListView
    private lateinit var textViewStatus: TextView

    private val peers = mutableListOf<WifiP2pDevice>()
    private lateinit var adapter: ArrayAdapter<String>

    private val LOCATION_PERMISSION_CODE = 100
    private val IMAGE_PICK_CODE = 200
    private val READ_MEDIA_PERMISSION_CODE = 300

    private var selectedDevice: WifiP2pDevice? = null
    private var connectedDeviceIpAddress: String? = null

    private var pendingImagePick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(16, 16, 16, 16)
        }

        buttonSearch = Button(this).apply {
            text = "Buscar dispositivos"
        }

        buttonSendImage = Button(this).apply {
            text = "Enviar Imagen"
            isEnabled = false
        }

        listView = ListView(this)
        textViewStatus = TextView(this)

        layout.addView(buttonSearch)
        layout.addView(listView)
        layout.addView(buttonSendImage)
        layout.addView(textViewStatus)

        setContentView(layout)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        listView.adapter = adapter

        intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

        receiver = WifiDirectReceiver(manager, channel, this)

        buttonSearch.setOnClickListener {
            if (checkLocationPermission()) {
                discoverPeers()
            } else {
                requestLocationPermission()
            }
        }

        buttonSendImage.setOnClickListener {
            if (connectedDeviceIpAddress != null) {
                if (checkReadMediaPermission()) {
                    pickImage()
                } else {
                    pendingImagePick = true
                    requestReadMediaPermission()
                }
            } else {
                Toast.makeText(this, "No hay ningún dispositivo conectado.", Toast.LENGTH_SHORT).show()
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            selectedDevice = peers[position]
            connectToDevice(selectedDevice!!)
            buttonSendImage.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    private fun discoverPeers() {
        if (!checkLocationPermission()) {
            requestLocationPermission()
            return
        }
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(this@MainActivity, "Buscando dispositivos...", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(this@MainActivity, "Error al buscar: $reason", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun connectToDevice(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }
        if (checkLocationPermission()) {
            manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Toast.makeText(this@MainActivity, "Conexión iniciada", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(this@MainActivity, "Fallo al conectar: $reason", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.NEARBY_WIFI_DEVICES,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun checkReadMediaPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestReadMediaPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), READ_MEDIA_PERMISSION_CODE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_MEDIA_PERMISSION_CODE)
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    discoverPeers()
                } else {
                    Toast.makeText(this, "Permiso de ubicación necesario para buscar dispositivos", Toast.LENGTH_SHORT).show()
                }
            }
            READ_MEDIA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    if (pendingImagePick) {
                        pendingImagePick = false
                        pickImage()
                    }
                } else {
                    Toast.makeText(this, "Permiso para leer imágenes necesario", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            data?.data?.let { uri ->
                val filePath = getPathFromUri(uri)
                connectedDeviceIpAddress?.let { ipAddress ->
                    if (filePath != null) {
                        Log.d("MainActivity", "Enviando imagen desde: $filePath a $ipAddress")
                        val fileClientSocket = FileClientSocket(ipAddress, filePath)
                        fileClientSocket.start()
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ruta del archivo.", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(this, "No hay dirección IP del dispositivo conectado.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getPathFromUri(uri: Uri): String? {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        var filePath: String? = null
        val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = it.getString(columnIndex)
            }
        }
        return filePath
    }

    fun updatePeers(peerList: Collection<WifiP2pDevice>) {
        peers.clear()
        peers.addAll(peerList)

        val deviceNames = peers.map { it.deviceName }
        adapter.clear()
        adapter.addAll(deviceNames)
        adapter.notifyDataSetChanged()
    }

    fun updateConnectionStatus(ipAddress: String) {
        textViewStatus.text = "Conectado a: $ipAddress"
        connectedDeviceIpAddress = ipAddress
        buttonSendImage.isEnabled = true
        Log.d("MainActivity", "Dirección IP del dispositivo conectado: $ipAddress")
    }

    fun updateDisconnectedStatus() {
        textViewStatus.text = "Desconectado"
        connectedDeviceIpAddress = null
        buttonSendImage.isEnabled = false
        Log.d("MainActivity", "Estado: Desconectado")
    }
}
