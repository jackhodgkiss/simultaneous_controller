package com.jackhodgkiss.simultaneous_controller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jackhodgkiss.simultaneous_controller.databinding.ActivityMainBinding
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions

class MainActivity : AppCompatActivity() {
    private val sensors: ArrayList<SensorItem> = ArrayList()
    private lateinit var sharedPreferences: SharedPreferences;
    private lateinit var binding: ActivityMainBinding
    private lateinit var notice_text_view: TextView
    private lateinit var sensor_recycler_view: RecyclerView
    private lateinit var swipe_container: SwipeRefreshLayout
    private lateinit var sensor_adapter: SensorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        notice_text_view = binding.noticeTextView
        sensor_recycler_view = binding.sensorRecyclerView
        swipe_container = binding.sensorSwipeContainer
        toggleNoticeVisibility()
        sensor_adapter = SensorAdapter(sensors)
        sensor_adapter.setHasStableIds(true)
        sensor_recycler_view.adapter = sensor_adapter
        sensor_recycler_view.layoutManager = LinearLayoutManager(this)
        swipe_container.setOnRefreshListener {
            scanForDevices(this)
        }
        sharedPreferences =
            this.getSharedPreferences(R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
    }

    private fun scanForDevices(context: Context) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null && !adapter.isEnabled) {
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT)
        } else {
            context.runWithPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ) {
                sensors.clear()
                sensor_adapter.notifyDataSetChanged()
                val settings =
                    ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
                adapter.bluetoothLeScanner.startScan(null, settings, callback)
                toggleNoticeVisibility(true)
                Handler(Looper.getMainLooper()).postDelayed({
                    adapter.bluetoothLeScanner.stopScan(callback)
                    swipe_container.isRefreshing = false
                    toggleNoticeVisibility()
                }, 10_000)
            }
        }
    }

    private val callback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result -> handleResult(result) }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { handleResult(result) }
        }
    }

    private fun handleResult(result: ScanResult) {
        if (!sensors.any { sensor -> sensor.address == result.device.address }) {
            if (result.device.name != null) {
                val sensor = SensorItem(
                    result.device.name,
                    result.device.address,
                    arrayListOf(result.rssi),
                    sharedPreferences.contains(result.device.address + "_is_favourite")
                )
                sensors.add(sensor)
                sensor_adapter.notifyItemChanged(sensors.size - 1)
            }
        } else {
            if (result.device.name != null) {
                val sensor = sensors.find { sensor -> sensor.address == result.device.address }
                sensor?.rssi_values?.add(result.rssi)
                sensor_adapter.notifyDataSetChanged()
            }
        }
    }

    private fun toggleNoticeVisibility(override: Boolean = false) {
        if (!override && sensors.isEmpty()) {
            notice_text_view.visibility = View.VISIBLE
        } else {
            notice_text_view.visibility = View.GONE
        }
    }

    companion object {
        const val REQUEST_ENABLE_BT: Int = 1
    }

}