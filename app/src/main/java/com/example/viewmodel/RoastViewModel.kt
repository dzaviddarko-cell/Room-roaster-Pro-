package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.Content
import com.example.data.GenerateContentRequest
import com.example.data.GenerationConfig
import com.example.data.InlineData
import com.example.data.Part
import com.example.data.RetrofitClient
import com.example.data.RoastRecord
import com.example.data.RoastRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class RoastViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RoastRepository

    init {
        val dao = AppDatabase.getDatabase(application).roastDao()
        repository = RoastRepository(dao)
    }

    val roastHistory: StateFlow<List<RoastRecord>> = repository.allRoasts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<RoastUiState>(RoastUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun roastRoom(bitmap: Bitmap, roomType: String, roastLevel: String) {
        _uiState.value = RoastUiState.Loading
        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _uiState.value = RoastUiState.Error("Please set your Gemini API Key in AI Studio Secrets.")
                    return@launch
                }

                val base64Image = bitmapToBase64(bitmap)
                
                val levelDescription = when(roastLevel.lowercase()) {
                    "light" -> "a gentle, playful tease"
                    "medium" -> "a solid comedic roast, point out flaws but keep it fun"
                    "spicy" -> "a brutal, savage roast that pulls no punches. Absolutely tear this room's interior design apart."
                    else -> "a funny roast"
                }

                val prompt = "This is a photo of a $roomType. Give me $levelDescription based on what you see in the photo. Keep it witty and under 3 sentences."

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = prompt),
                                Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(temperature = 0.9f)
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (textResponse != null) {
                    val record = RoastRecord(
                        roomType = roomType,
                        roastLevel = roastLevel,
                        roastText = textResponse
                    )
                    repository.insert(record)
                    _uiState.value = RoastUiState.Success(record)
                } else {
                    _uiState.value = RoastUiState.Error("Could not generate a roast. Try again.")
                }
            } catch (e: Exception) {
                _uiState.value = RoastUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = RoastUiState.Idle
    }

    fun deleteRoast(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val scaledBitmap = scaleBitmapDown(bitmap, 1024)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val maxDimensionF = maxDimension.toFloat()
        val bitWidth = bitmap.width.toFloat()
        val bitHeight = bitmap.height.toFloat()
        
        if (bitWidth <= maxDimensionF && bitHeight <= maxDimensionF) {
            return bitmap
        }
        
        val ratio = (maxDimensionF / bitWidth).coerceAtMost(maxDimensionF / bitHeight)
        val width = (ratio * bitWidth).toInt()
        val height = (ratio * bitHeight).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}

sealed class RoastUiState {
    object Idle : RoastUiState()
    object Loading : RoastUiState()
    data class Success(val roast: RoastRecord) : RoastUiState()
    data class Error(val message: String) : RoastUiState()
}
