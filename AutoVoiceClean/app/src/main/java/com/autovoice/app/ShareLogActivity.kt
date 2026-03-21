package com.autovoice.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class ShareLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val logFile = File(getExternalFilesDir(null), "autovoice_log.txt")
        if (!logFile.exists()) { finish(); return }
        try {
            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", logFile)
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "AutoVoice Log")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Share Log"))
        } catch (_: Exception) {}
        finish()
    }
}
