package com.ajan.turkceses

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : Activity() {
    private val requestSpeech = 1001
    private val requestMic = 1002
    private lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this).apply {
            text = "AJAN — TÜRKÇE SES KÖPRÜSÜ\nDil: Türkçe (Türkiye) · tr-TR"
            textSize = 20f
            setPadding(24, 32, 24, 20)
        }
        resultView = TextView(this).apply {
            text = "Henüz konuşma alınmadı."
            textSize = 18f
            setPadding(24, 24, 24, 24)
        }
        val listenButton = Button(this).apply {
            text = "TÜRKÇE DİNLE"
            setOnClickListener { checkMicAndListen() }
        }
        val copyButton = Button(this).apply {
            text = "METNİ TEKRAR KOPYALA"
            setOnClickListener { copyResult() }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            addView(title)
            addView(listenButton)
            addView(resultView)
            addView(copyButton)
        }
        setContentView(layout)
    }

    private fun checkMicAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), requestMic
            )
        } else {
            startTurkishRecognition()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestMic && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTurkishRecognition()
        } else if (requestCode == requestMic) {
            Toast.makeText(this, "Mikrofon izni gerekli.", Toast.LENGTH_LONG).show()
        }
    }

    private fun startTurkishRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Türkçe konuşabilirsiniz")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            startActivityForResult(intent, requestSpeech)
        } catch (e: Exception) {
            Toast.makeText(this, "Cihazda konuşma tanıma hizmeti bulunamadı.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated by Android, retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestSpeech && resultCode == RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()?.trim().orEmpty()
            resultView.text = if (spoken.isNotBlank()) spoken else "Ses tanıma boş döndü."
            if (spoken.isNotBlank()) copyResult()
        }
    }

    private fun copyResult() {
        val text = resultView.text.toString()
        if (text.isBlank() || text == "Henüz konuşma alınmadı." || text == "Ses tanıma boş döndü.") return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("ajan_turkce_metin", text))
        Toast.makeText(this, "Türkçe metin panoya kopyalandı.", Toast.LENGTH_SHORT).show()
    }
}
