package com.example.wifidirect

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pInfo
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

class WifiDirectReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val activity: MainActivity
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                Log.d("WifiDirectReceiver", "WIFI_P2P_STATE_CHANGED_ACTION. Estado: $state")
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                Log.d("WifiDirectReceiver", "Evento WIFI_P2P_PEERS_CHANGED_ACTION recibido.")
                val fineLocationGranted = ActivityCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val coarseLocationGranted = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val nearbyWifiDevicesGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

                if (fineLocationGranted && coarseLocationGranted && nearbyWifiDevicesGranted) {
                    Log.d("WifiDirectReceiver", "Se tienen todos los permisos necesarios. Solicitando peers.")
                    manager.requestPeers(channel) { peers: WifiP2pDeviceList ->
                        Log.d("WifiDirectReceiver", "Callback de requestPeers recibido. Número de peers: ${peers.deviceList.size}")
                        activity.updatePeers(peers.deviceList)
                    }
                } else {
                    Log.w("WifiDirectReceiver", "No se tienen todos los permisos necesarios para solicitar peers.")
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                Log.d("WifiDirectReceiver", "Evento WIFI_P2P_CONNECTION_CHANGED_ACTION recibido.")
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                if (networkInfo != null) {
                    Log.d("WifiDirectReceiver", "Estado de la red: ${networkInfo.state}, Conectado: ${networkInfo.isConnected}")
                    if (networkInfo.isConnected) {
                        Log.d("WifiDirectReceiver", "Conectado. Solicitando información de conexión.")
                        manager.requestConnectionInfo(channel) { info: WifiP2pInfo? ->
                            val hostAddress = info?.groupOwnerAddress?.hostAddress ?: "Desconocido"
                            Log.d("WifiDirectReceiver", "Información de conexión disponible. Grupo Formado: ${info?.groupFormed}, Owner: ${info?.isGroupOwner}, Owner Address: $hostAddress")
                            activity.updateConnectionStatus(hostAddress)
                        }
                    } else {
                        Log.d("WifiDirectReceiver", "Desconectado o conexión perdida.")
                        activity.updateDisconnectedStatus()
                    }
                } else {
                    Log.w("WifiDirectReceiver", "Información de red nula en CONNECTION_CHANGED_ACTION.")
                    activity.updateDisconnectedStatus() // Considera un estado de error más específico si es necesario
                }
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                Log.d("WifiDirectReceiver", "Evento WIFI_P2P_THIS_DEVICE_CHANGED_ACTION recibido.")
                val device = intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                Log.d("WifiDirectReceiver", "Información de este dispositivo: ${device?.deviceName} (${device?.deviceAddress})")
            }
        }
    }
}