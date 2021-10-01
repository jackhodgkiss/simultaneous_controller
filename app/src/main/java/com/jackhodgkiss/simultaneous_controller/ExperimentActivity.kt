package com.jackhodgkiss.simultaneous_controller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.ActivityExperimentBinding

class ExperimentActivity : AppCompatActivity() {
    private lateinit var manifest: ExperimentManifest
    private lateinit var binding: ActivityExperimentBinding
    private lateinit var experimentSensorRecyclerView: RecyclerView
    private lateinit var experimentSensorAdapter: ExperimentSensorAdapter
    private val experimentSensors = mutableListOf<ExperimentSensorItem>()

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
        binding.connectButton.setOnClickListener { connectDevices() }
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

    private fun connectDevices() {
        val sensorAddress = experimentSensors[0].address
        val bluetoothManager = this.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(sensorAddress)
        bluetoothDevice.connectGatt(this, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.d("BLE", "Connection State Changed for ${gatt?.device?.address}")
        }

    }

    private fun startExperiment() {
        setChronometer()
        binding.timeChronometer.start()
    }

    private fun experimentFinished() {

    }
}