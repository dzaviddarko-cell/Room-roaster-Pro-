package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.RoomRoasterApp
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.SpeechManager
import com.example.viewmodel.RoastViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RoastViewModel by viewModels()
    private lateinit var speechManager: SpeechManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        speechManager = SpeechManager(this)

        setContent {
            MyApplicationTheme {
                RoomRoasterApp(viewModel, speechManager)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.shutdown()
    }
}
