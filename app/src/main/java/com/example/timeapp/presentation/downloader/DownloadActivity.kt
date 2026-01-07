package com.example.timeapp.presentation.downloader

import android.app.DownloadManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme

class DownloadActivity : ComponentActivity() {
    private val viewModel: DownloadViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CHECK", "DownloadActivity onCreate called!")

        setContent {
            MaterialTheme {
                DownloadScreen(viewModel = viewModel)
            }
        }
    }
}