package com.example.timeapp.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.timeapp.data.TimerRepositoryImpl
import com.example.timeapp.databinding.ActivityTimerBinding
import com.example.timeapp.domain.TimerUseCase
import kotlinx.coroutines.launch

class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding

    private val viewModel: TimerViewModel by viewModels {
        val useCase = TimerUseCase(TimerRepositoryImpl())
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TimerViewModel(useCase) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeState()
        observeEvent()
        setupClickListeners()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    binding.textTime.text = (state.time / 1000).toString()
                    binding.btnStart.isEnabled = !state.isRunning
                    binding.btnStop.isEnabled = state.isRunning
                    binding.btnResume.isEnabled = !state.isRunning && state.time > 0
                }
            }
        }
    }

    private fun observeEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is TimerEvent.ShowTimerStartToast ->
                            Toast.makeText(
                                this@TimerActivity,
                                "Timer Started",
                                Toast.LENGTH_SHORT
                            ).show()

                        is TimerEvent.ShowTimerStopToast ->
                            Toast.makeText(
                                this@TimerActivity,
                                "Timer Stopped",
                                Toast.LENGTH_SHORT
                            ).show()

                        is TimerEvent.ShowTimerResetToast ->
                            Toast.makeText(
                                this@TimerActivity,
                                "Timer Reset",
                                Toast.LENGTH_SHORT
                            ).show()

                        is TimerEvent.ShowTimerResumeToast ->
                            Toast.makeText(
                                this@TimerActivity,
                                "Timer Resumed",
                                Toast.LENGTH_SHORT
                            ).show()
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
}
