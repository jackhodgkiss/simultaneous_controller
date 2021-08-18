package com.jackhodgkiss.simultaneous_controller

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.jackhodgkiss.simultaneous_controller.databinding.ActivityExperimentBinding

class ExperimentActivity : AppCompatActivity() {

    lateinit var manifest: ExperimentManifest
    lateinit var binding: ActivityExperimentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExperimentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manifest = intent.getParcelableExtra<ExperimentManifest>("Manifest")!!
        binding.gestureTextView.text = manifest.gesture.toString()
        setChronometer()
        binding.timeChronometer.setOnChronometerTickListener {
            if ("00:00" == binding.timeChronometer.text) {
                experimentFinished()
            }
        }
        binding.startButton.setOnClickListener { startExperiment() }
    }

    private fun setChronometer() {
        when (manifest.experimentDuration) {
            ExperimentDuration.THIRTY_SECONDS -> binding.timeChronometer.base =
                SystemClock.elapsedRealtime() + (30 * 1000)
            ExperimentDuration.SIXTY_SECONDS -> binding.timeChronometer.base =
                SystemClock.elapsedRealtime() + (60 * 1000)
            ExperimentDuration.NINETY_SECONDS -> binding.timeChronometer.base =
                SystemClock.elapsedRealtime() + (90 * 1000)
        }
    }

    private fun startExperiment() {
        setChronometer()
        binding.timeChronometer.start()
    }

    private fun experimentFinished() {

    }
}