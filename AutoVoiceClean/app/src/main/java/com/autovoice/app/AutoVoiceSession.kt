package com.autovoice.app

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class AutoVoiceSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen = MainCarScreen(carContext)
}
