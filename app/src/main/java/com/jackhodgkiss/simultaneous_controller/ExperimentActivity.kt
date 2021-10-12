package com.jackhodgkiss.simultaneous_controller

import android.bluetooth.*
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.ActivityExperimentBinding
import java.util.*
import kotlin.collections.ArrayList

class ExperimentActivity : AppCompatActivity() {
    private lateinit var manifest: ExperimentManifest
    private lateinit var binding: ActivityExperimentBinding
    private lateinit var experimentSensorRecyclerView: RecyclerView
    private lateinit var experimentSensorAdapter: ExperimentSensorAdapter
    private val experimentSensors = ArrayList<ExperimentSensorItem>()
    private lateinit var bluetoothGatt: BluetoothGatt
    private var countDownEnd: Long = 0L

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExperimentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manifest = intent.getParcelableExtra("Manifest")!!
        initUI()
    }

    private fun initUI() {
        binding.gestureTextView.text = manifest.gesture.toString()
        experimentSensorRecyclerView = binding.sensorRecyclerView
        experimentSensorAdapter = ExperimentSensorAdapter(experimentSensors)
        experimentSensorAdapter.setHasStableIds(true)
        experimentSensorRecyclerView.adapter = experimentSensorAdapter
        experimentSensorRecyclerView.layoutManager = LinearLayoutManager(this)
        experimentSensorRecyclerView.itemAnimator = null
        binding.startButton.setOnClickListener { startExperiment() }
        binding.timeChronometer.setOnChronometerTickListener {
            if(binding.timeChronometer.base <= countDownEnd) {
                binding.timeChronometer.stop()
            }
        }
        setChronometer()
    }

    private fun startExperiment() {
        setChronometer()
        binding.timeChronometer.start()
    }

    private fun setChronometer() {
        countDownEnd = SystemClock.elapsedRealtime()
        binding.timeChronometer.isCountDown = true
        binding.timeChronometer.base = SystemClock.elapsedRealtime() + 10 * 1001
    }
}