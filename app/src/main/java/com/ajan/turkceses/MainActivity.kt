package com.ajan.turkceses

import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private var recognizer: SpeechRecognizer? = null
    private lateinit var txt: TextView
    private var dinliyor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(40, 80, 40, 40)
            setBackgroundColor(Color.WHITE)
        }
        val title = TextView(this).apply {
            text = "AJAN - TURKCE SES KOPRUSU\nDil: Turkce - tr-TR"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        txt = TextView(this).apply {
            text = "Baslatiliyor..."
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 40)
        }
        val btn = Button(this).apply {
            text = "TEKRAR DINLE"
            textSize = 18f
            setOnClickListener { dinlemeyiBaslat() }
        }
        lay.addView(title)
        lay.addView(btn)
        lay.addView(txt)
        setContentView(lay)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            dinlemeyiBaslat()
        }
    }

    override fun onRequestPermissionsResult(rc: Int, perms: Array<String>, res: IntArray) {
        super.onRequestPermissionsResult(rc, perms, res)
        if (res.isNotEmpty() && res[0] == PackageManager.PERMISSION_GRANTED) dinlemeyiBaslat()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun dinlemeyiBaslat() {
        if (dinliyor) return
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            txt.text = "Ses tanima yok"
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
            override fun onReadyForSpeech(p: Bundle?)
            {
                dinliyor = true
                txt.text = "Dinliyorum... konus kanka"
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(e: Int) {
                dinliyor = false
                txt.text = "Dinleme bitti, tekrar için butona bas"
            }
            override fun onResults(r: Bundle?) {
                dinliyor = false
                val list = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val metin = list?.firstOrNull() ?: ""
                if (metin.isNotEmpty()) {
                    txt.text = metin
                    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("ajan", metin))
                }
                txt.text = txt.text.toString() + "\n(Tekrar için butona bas)"
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
