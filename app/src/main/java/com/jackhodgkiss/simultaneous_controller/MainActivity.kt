package com.jackhodgkiss.simultaneous_controller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jackhodgkiss.simultaneous_controller.databinding.ActivityMainBinding
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions

class MainActivity : AppCompatActivity() {
    private val sensors: ArrayList<SensorItem> = ArrayList()
    private lateinit var binding: ActivityMainBinding
    private lateinit var swipe_container: SwipeRefreshLayout
    private lateinit var sensor_adapter: SensorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(sensors.isEmpty()) {
            binding.noticeTextView.visibility = View.VISIBLE
        } else {
            binding.noticeTextView.visibility = View.GONE
        }
        sensor_adapter = SensorAdapter(sensors)
        binding.sensorRecyclerView.adapter = sensor_adapter
        binding.sensorRecyclerView.layoutManager = LinearLayoutManager(this)
        swipe_container = binding.sensorSwipeContainer
        swipe_container.setOnRefreshListener {
            scanForDevices(this)
        }
    }

    fun scanForDevices(context: Context) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if(adapter != null && !adapter.isEnabled) {
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT)
        } else {
            context.runWithPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN) {
                adapter.bluetoothLeScanner.startScan(callback)
                binding.noticeTextView.visibility = View.GONE
                Handler(Looper.getMainLooper()).postDelayed({
                    adapter.bluetoothLeScanner.stopScan(callback)
                    swipe_container.isRefreshing = false
                    if(sensors.isEmpty()) {
                        binding.noticeTextView.visibility = View.VISIBLE
                    } else {
                        binding.noticeTextView.visibility = View.GONE
                    }
                }, 10_000)
            }
        }
    }

    private val callback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result -> deviceFound(result.device) }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { deviceFound(result.device) }
        }
    }

    private fun deviceFound(device: BluetoothDevice) {
        if(!sensors.any{ sensor -> sensor.address == device.address }) {
            if (device.name != null) {
                val sensor = SensorItem(device.name, device.address)
                sensors.add(sensor)
                sensor_adapter.notifyItemChanged(sensors.size - 1)
            }
        }
    }

    companion object {
        const val REQUEST_ENABLE_BT: Int = 1
    }

}