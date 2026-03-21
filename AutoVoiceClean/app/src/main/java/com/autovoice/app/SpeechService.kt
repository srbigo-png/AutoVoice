package com.autovoice.app

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechService : Service(), TextToSpeech.OnInitListener {

    companion object {
        const val ACTION_START = "com.autovoice.START"
        const val ACTION_STOP = "com.autovoice.STOP"
        const val BROADCAST_STATUS = "com.autovoice.STATUS"
    }

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var lastCmd = 0L
    private val COOLDOWN = 2000L

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecognition()
            ACTION_STOP -> stopRecognition()
        }
        return START_NOT_STICKY
    }

    private fun startRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            broadcast("Speech recognition not available")
            return
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) handleCommand(matches[0])
            }
            override fun onError(error: Int) { broadcast("Could not hear you, try again") }
            override fun onReadyForSpeech(p: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p: Bundle?) {}
            override fun onEvent(t: Int, p: Bundle?) {}
        })
        recognizer!!.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        })
    }

    private fun stopRecognition() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }

    private fun handleCommand(raw: String) {
        val now = System.currentTimeMillis()
        if (now - lastCmd < COOLDOWN) return
        lastCmd = now
        val cmd = sanitize(raw.lowercase())
        if (cmd.isEmpty()) return

        when {
            cmd.contains("\u05e0\u05d5\u05d5\u05d8") || cmd.contains("\u05e7\u05d7 \u05d0\u05d5\u05ea\u05d9") || cmd.contains("\u05dc\u05da \u05dc") -> {
                val dest = extractAfter(cmd, listOf("\u05e0\u05d5\u05d5\u05d8 \u05dc", "\u05e0\u05d5\u05d5\u05d8 \u05d0\u05dc", "\u05e7\u05d7 \u05d0\u05d5\u05ea\u05d9 \u05dc", "\u05dc\u05da \u05dc"))
                if (dest.isNotEmpty()) { say("\u05de\u05e0\u05d5\u05d5\u05d8 \u05dc $dest"); wazeNavigate(dest); broadcast("Navigating to $dest") }
                else broadcast("Where to navigate?")
            }
            cmd.contains("\u05d7\u05d9\u05d9\u05d2") || cmd.contains("\u05d4\u05ea\u05e7\u05e9\u05e8") -> {
                val name = extractAfter(cmd, listOf("\u05d7\u05d9\u05d9\u05d2 \u05dc", "\u05d7\u05d9\u05d9\u05d2 \u05d0\u05dc", "\u05d4\u05ea\u05e7\u05e9\u05e8 \u05dc", "\u05d4\u05ea\u05e7\u05e9\u05e8 \u05d0\u05dc"))
                if (name.isNotEmpty()) { say("\u05de\u05d7\u05d9\u05d9\u05d2 \u05dc $name"); dial(name); broadcast("Calling $name") }
                else broadcast("Who to call?")
            }
            cmd.contains("\u05d7\u05e4\u05e9") -> {
                val q = extractAfter(cmd, listOf("\u05d7\u05e4\u05e9 \u05d0\u05ea ", "\u05d7\u05e4\u05e9 "))
                if (q.isNotEmpty()) { say("\u05de\u05d7\u05e4\u05e9 $q"); wazeSearch(q); broadcast("Searching $q") }
                else broadcast("What to search?")
            }
            cmd.contains("\u05d5\u05d5\u05d9\u05d6") || cmd.contains("waze") -> {
                say("\u05e4\u05d5\u05ea\u05d7 \u05d5\u05d5\u05d9\u05d6"); launchWaze(); broadcast("Opening Waze")
            }
            else -> broadcast("Not understood - try again")
        }
    }

    private fun wazeNavigate(dest: String) = startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse("waze://?q=${Uri.encode(dest)}&navigate=yes"))
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })

    private fun wazeSearch(q: String) = startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse("waze://?q=${Uri.encode(q)}"))
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })

    private fun launchWaze() {
        val i = packageManager.getLaunchIntentForPackage("com.waze")
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK } ?: return
        startActivity(i)
    }

    private fun dial(name: String) = startActivity(
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(name)}"))
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })

    private fun sanitize(s: String) = s.replace(Regex("[<>\"'%;()&+\\\\]"), "").take(300).trim()

    private fun extractAfter(text: String, kws: List<String>): String {
        for (kw in kws) {
            val i = text.indexOf(kw)
            if (i != -1) return text.substring(i + kw.length).trim()
        }
        return ""
    }

    private fun say(text: String) = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    private fun broadcast(status: String) = sendBroadcast(Intent(BROADCAST_STATUS).putExtra("status", status))

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale("he", "IL")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopRecognition()
        tts?.shutdown()
        super.onDestroy()
    }
}
