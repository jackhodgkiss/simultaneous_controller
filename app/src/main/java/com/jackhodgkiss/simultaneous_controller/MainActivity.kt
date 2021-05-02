package com.jackhodgkiss.simultaneous_controller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scanForDevices(this)
    }

    fun scanForDevices(context: Context) {
        val adapter = BluetoothAdapter.getDefaultAdapter()

        context.runWithPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN) {
            adapter.bluetoothLeScanner.startScan(callback)
            Handler(Looper.getMainLooper()).postDelayed({
                adapter.bluetoothLeScanner.stopScan(callback)
                Log.d("BLE", "Scanning Stopped")
            }, 10_000)
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
        Log.d("BLE", device.address)
    }

}