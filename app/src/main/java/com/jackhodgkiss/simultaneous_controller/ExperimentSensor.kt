package com.jackhodgkiss.simultaneous_controller

import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ExperimentSensor(
    private val context: Context,
    var address: String,
    private val connectionManager: ConnectionManager
) {
    var isConnected: Boolean = false
    var bluetoothDevice: BluetoothDevice? = null
    var bluetoothGATT: BluetoothGatt? = null
    var bluetoothManager: BluetoothManager? = null
    var bluetoothAdapter: BluetoothAdapter? = null

    init {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager!!.adapter
        bluetoothDevice = bluetoothAdapter?.getRemoteDevice(address)
    }

    fun connect() {
        bluetoothGATT = bluetoothDevice?.connectGatt(context, false, gattCallback)
    }

    val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Successfully connected to $address"
                        )
                        isConnected = true
                        address = "HELLO"
                        (context as ExperimentActivity).runOnUiThread {
                            Runnable {
                                context.updateAdapter()
                            }.run()
                        }
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Successfully disconnected from $address"
                        )
                        bluetoothGATT?.close()
                        isConnected = false
                    }
                    else -> {
                        Log.e("BluetoothGattCallback", "Error $status encountered from $address")
                        bluetoothGATT?.close()
                        isConnected = false
                    }
                }
            }
        }
    }

}