package com.autovoice.app

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class AutoVoiceCarService : CarAppService() {
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_DEBUG_HOSTS
    override fun onCreateSession(): Session = AutoVoiceSession()
}
