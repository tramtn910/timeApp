package com.example.timeapp.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.timeapp.databinding.ActivityTimerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding
    private val viewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeState()
        observeEvent()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.processIntent(TimerIntent.GetCurrentTime)
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collectLatest { state ->
                    binding.textTime.text = (state.time / 1000).toString()
                    binding.textCurrentTime.text = if (state.currentTime.isNotEmpty()) {
                        "${state.currentTime} (${state.timeZone})"
                    } else {
                        "Loading time..."
                    }
                    updateButtonsState(state)
                }
            }
        }
    }

    private fun observeEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collectLatest { event ->
                    when (event) {
                        is TimerEvent.ShowTimerStartToast -> showToast("Timer Started")
                        is TimerEvent.ShowTimerStopToast -> showToast("Timer Stopped")
                        is TimerEvent.ShowTimerResetToast -> showToast("Timer Reset")
                        is TimerEvent.ShowTimerResumeToast -> showToast("Timer Resumed")
                        is TimerEvent.ShowCurrentTimeToast -> showToast("Current Time Updated")
                        is TimerEvent.ShowErrorToast -> showToast(event.message)
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
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

    private fun updateButtonsState(state: TimerViewState) {
        binding.btnStart.isEnabled = !state.isRunning
        binding.btnStop.isEnabled = state.isRunning
        binding.btnResume.isEnabled = !state.isRunning && state.time > 0
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}