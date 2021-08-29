package com.jackhodgkiss.simultaneous_controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.ActivityExperimentBinding

class ExperimentActivity : AppCompatActivity() {
    private lateinit var manifest: ExperimentManifest
    private lateinit var binding: ActivityExperimentBinding
    private lateinit var experimentSensorRecyclerView: RecyclerView
    private lateinit var experimentSensorAdapter: ExperimentSensorAdapter

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
        val experimentSensors = mutableListOf<ExperimentSensorItem>()
        for (sensor in manifest.selectedSensors) {
            experimentSensors.add(ExperimentSensorItem(sensor.value, sensor.key))
        }
        experimentSensorRecyclerView = binding.sensorRecyclerView
        experimentSensorAdapter =
            ExperimentSensorAdapter(experimentSensors as ArrayList<ExperimentSensorItem>)
        experimentSensorAdapter.setHasStableIds(true)
        experimentSensorRecyclerView.adapter = experimentSensorAdapter
        experimentSensorRecyclerView.layoutManager = LinearLayoutManager(this)
        experimentSensorRecyclerView.itemAnimator = null
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