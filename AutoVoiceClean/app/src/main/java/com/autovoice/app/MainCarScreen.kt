package com.autovoice.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.content.ContextCompat

class MainCarScreen(carContext: CarContext) : Screen(carContext) {

    private var statusText = "Press mic to speak"
    private var isListening = false

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val msg = intent?.getStringExtra("status") ?: return
            isListening = false
            statusText = msg
            invalidate()
        }
    }

    init {
        carContext.registerReceiver(
            statusReceiver,
            IntentFilter(SpeechService.BROADCAST_STATUS),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onGetTemplate(): Template {
        val micAction = Action.Builder()
            .setTitle(if (isListening) "Stop" else "Speak")
            .setOnClickListener { if (isListening) stopListening() else startListening() }
            .build()

        val wazeAction = Action.Builder()
            .setTitle("Waze")
            .setOnClickListener { openWaze() }
            .build()

        val dialAction = Action.Builder()
            .setTitle("Dial")
            .setOnClickListener { openDialer() }
            .build()

        val helpStrip = ActionStrip.Builder()
            .addAction(Action.Builder()
                .setTitle("Help")
                .setOnClickListener { screenManager.push(HelpCarScreen(carContext)) }
                .build())
            .build()

        return MessageTemplate.Builder(statusText)
            .setTitle("AutoVoice")
            .setActionStrip(helpStrip)
            .addAction(micAction)
            .addAction(wazeAction)
            .addAction(dialAction)
            .build()
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(carContext, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            statusText = "Grant microphone permission on phone"
            invalidate()
            return
        }
        isListening = true
        statusText = "Listening... speak now"
        invalidate()
        carContext.startService(Intent(carContext, SpeechService::class.java).apply {
            action = SpeechService.ACTION_START
        })
    }

    private fun stopListening() {
        isListening = false
        statusText = "Press mic to speak"
        invalidate()
        carContext.startService(Intent(carContext, SpeechService::class.java).apply {
            action = SpeechService.ACTION_STOP
        })
    }

    private fun openWaze() {
        try {
            carContext.startActivity(Intent(Intent.ACTION_VIEW,
                android.net.Uri.parse("waze://")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            statusText = "Opening Waze..."
        } catch (e: Exception) {
            statusText = "Waze not installed"
        }
        invalidate()
    }

    private fun openDialer() {
        try {
            carContext.startActivity(Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            statusText = "Cannot open dialer"
            invalidate()
        }
    }

    override fun onDestroy() {
        try { carContext.unregisterReceiver(statusReceiver) } catch (_: Exception) {}
        super.onDestroy()
    }
}
