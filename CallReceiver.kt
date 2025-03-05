package com.example.telefonia

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("CallReceiver", "Broadcast recibido")

        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            // Obtener el número de la llamada entrante
            val incomingNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            } else {
                intent.extras?.getString("incoming_number")
            }

            Log.d("CallReceiver", "Estado: $state, Numero entrante: $incomingNumber")

            if (state == TelephonyManager.EXTRA_STATE_RINGING && !incomingNumber.isNullOrBlank()) {
                context?.let {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(it)
                    val savedNumber = prefs.getString("saved_number", "")
                    val autoReplyMessage = prefs.getString("auto_reply_message", "Estoy ocupado, te respondere luego.")

                    if (incomingNumber == savedNumber) {
                        Log.d("CallReceiver", "Numero coincide, enviando SMS...")
                        sendSMS(incomingNumber, autoReplyMessage ?: "", it)
                    } else {
                        Log.d("CallReceiver", "Numero no coincide, no se enviara SMS")
                    }
                }
            }
        }
    }

    private fun sendSMS(phoneNumber: String, message: String, context: Context) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)

            Log.d("CallReceiver", "SMS enviado a: $phoneNumber con mensaje: $message")
            Toast.makeText(context, "Mensaje enviado a $phoneNumber", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("CallReceiver", "⚠Error al enviar SMS: ${e.message}")
            Toast.makeText(context, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
        }
    }
}
