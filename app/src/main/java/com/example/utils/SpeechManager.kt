package com.example.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class SpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale.US
        } else {
            Log.e("SpeechManager", "TTS Initialization failed")
        }
    }

    fun getAvailableVoices(): List<Voice> {
        if (!isInitialized) return emptyList()
        return try {
            tts?.voices?.filter { it.locale.language == Locale.ENGLISH.language }?.take(5) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun speak(text: String, voice: Voice? = null) {
        if (!isInitialized) return
        
        voice?.let {
            tts?.voice = it
        } ?: run {
            tts?.language = Locale.US
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RoastSpeaker")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
