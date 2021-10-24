package com.jackhodgkiss.simultaneous_controller

import android.bluetooth.*
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.ActivityExperimentBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class ExperimentActivity : AppCompatActivity() {
    private lateinit var manifest: ExperimentManifest
    private lateinit var binding: ActivityExperimentBinding
    private lateinit var experimentSensorRecyclerView: RecyclerView
    private lateinit var experimentSensorAdapter: ExperimentSensorAdapter
    private lateinit var connectionManager: ConnectionManager
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
        connectionManager =
            ConnectionManager(this, manifest.selectedSensors.keys.toList())
        initUI()
    }

    private fun initUI() {
        binding.gestureTextView.text = manifest.gesture.toString()
        experimentSensorRecyclerView = binding.sensorRecyclerView
        experimentSensorAdapter = ExperimentSensorAdapter(connectionManager)
        experimentSensorAdapter.setHasStableIds(true)
        experimentSensorRecyclerView.adapter = experimentSensorAdapter
        experimentSensorRecyclerView.layoutManager = LinearLayoutManager(this)
        experimentSensorRecyclerView.itemAnimator = null
        binding.connectButton.setOnClickListener { connectionManager.connectAll() }
        binding.startButton.setOnClickListener { startExperiment() }
        binding.timeChronometer.setOnChronometerTickListener {
            if (binding.timeChronometer.base <= countDownEnd) {
                binding.timeChronometer.stop()
            }
        }
        setChronometer()
        updateAdapter()
    }

    private fun startExperiment() {
//        setChronometer()
//        binding.timeChronometer.start()
        experimentLoop()
    }

    private fun experimentLoop() {
        var packetsRemaining = 4
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (--packetsRemaining <= 0) {
                    cancel()
                }
                connectionManager.enqueueOperation(
                    OperationPair(
                        connectionManager.sensors[0].address,
                        Operation.CharacteristicWrite,
                        packetsRemaining.toString().toByteArray(Charsets.US_ASCII)
                    )
                )
            }
        }, 0, 50)
    }

    fun updateAdapter() {
        experimentSensorAdapter.notifyDataSetChanged()
    }

    private fun setChronometer() {
        countDownEnd = SystemClock.elapsedRealtime()
        binding.timeChronometer.isCountDown = true
        binding.timeChronometer.base = SystemClock.elapsedRealtime() + 10 * 1001
    }
}