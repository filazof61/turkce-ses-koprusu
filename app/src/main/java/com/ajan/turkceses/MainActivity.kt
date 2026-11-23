package com.ajan.turkceses

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var recognizer: SpeechRecognizer? = null
    private lateinit var txt: TextView
    private var dinliyor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txt = findViewById(R.id.txtResult)
        val btn = findViewById<Button>(R.id.btnListen)
        btn.text = "OTOMATIK DINLIYOR - KONUŞ"
        // Izin iste
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            dinlemeyiBaslat()
        }
        // Buton da yedekte dursun
        btn.setOnClickListener { dinlemeyiBaslat() }
    }

    override fun onRequestPermissionsResult(rc: Int, perms: Array<String>, res: IntArray) {
        super.onRequestPermissionsResult(rc, perms, res)
        if (res.isNotEmpty() && res[0] == PackageManager.PERMISSION_GRANTED) dinlemeyiBaslat()
    }

    override fun onResume() {
        super.onResume()
        // Güç tuşu çift bas -> uygulama ön plana gelince tekrar dinle
        if (!dinliyor) dinlemeyiBaslat()
    }

    private fun dinlemeyiBaslat() {
        if (dinliyor) return
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            txt.text = "Ses tanıma yok"
            return
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) { dinliyor = true; txt.text = "Dinliyorum... konuş kanka" }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(e: Int) {
                dinliyor = false
                // Hata olsa bile 500ms sonra tekrar dinle - sürekli dinleme
                txt.postDelayed({ dinlemeyiBaslat() }, 500)
            }
            override fun onResults(r: Bundle?) {
                dinliyor = false
                val list = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val metin = list?.firstOrNull() ?: ""
                if (metin.isNotEmpty()) {
                    txt.text = metin
                    // Panoya kopyala - Termux ajanı okuyacak
                    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("ajan", metin))
                }
                // Hemen tekrar dinle - butonsuz sürekli
                txt.postDelayed({ dinlemeyiBaslat() }, 300)
            }
            override fun onPartialResults(r: Bundle?) {
                val list = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!list.isNullOrEmpty()) txt.text = list[0]
            }
            override fun onEvent(a: Int, b: Bundle?) {}
        })
        recognizer?.startListening(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizer?.destroy()
    }
}
