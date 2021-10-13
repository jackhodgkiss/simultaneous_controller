package com.jackhodgkiss.simultaneous_controller

import android.bluetooth.*
import android.content.Context
import android.util.Log

class ExperimentSensor(
    private val context: Context,
    var address: String,
    private val connectionManager: ConnectionManager
) {
    val name: String =
        context.getSharedPreferences(R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
            .getString(address + "_name", address).toString()
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

    fun disconnect() {
        bluetoothGATT?.disconnect()
    }

    fun discoverServices() {
        bluetoothGATT?.discoverServices()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Successfully connected to $address"
                        )
                        isConnected = true
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
                (context as ExperimentActivity).runOnUiThread {
                    Runnable {
                        context.updateAdapter()
                    }.run()
                }
                connectionManager.finishOperation()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(bluetoothGATT) {
                Log.i(
                    "BluetoothGattCallback",
                    "Discovered ${this?.services?.size} services for $address"
                )
                bluetoothGATT?.printGattTable()
            }
            if (connectionManager.currentOperationPair?.operation == Operation.DiscoverServices) {
                connectionManager.finishOperation()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {

        }
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.d("BluetoothGattTable", "No services and characteristic available")
            return
        }
        Log.i("BluetoothGattTable", "${services.size} services found for $address")
        services.forEach { service ->
            val characteristicTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i(
                "BluetoothGattTable",
                "\nService ${service.uuid}\nCharacteristics:\n$characteristicTable"
            )
        }
    }

}