package com.example.timeapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.timeapp.databinding.ActivityMainBinding
import com.example.timeapp.mvi.TimerIntent
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TimerViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    binding.textTime.text = (state.time / 1000).toString()
                }
            }
        }

        binding.btnStart.setOnClickListener {
            viewModel.processIntent(TimerIntent.Start)
        }
        binding.btnStop.setOnClickListener {
            viewModel.processIntent(TimerIntent.Stop)
        }
        binding.btnReset.setOnClickListener {
            viewModel.processIntent(TimerIntent.Reset)
        }
        binding.btnResume.setOnClickListener {
            viewModel.processIntent(TimerIntent.Resume)
        }
    }
}