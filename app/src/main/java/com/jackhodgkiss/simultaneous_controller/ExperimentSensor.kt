package com.jackhodgkiss.simultaneous_controller

import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*

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
    private val serialServiceUUID = UUID.fromString("f000c0c0-0451-4000-b000-000000000000")
    private val dataInUUID = UUID.fromString("f000c0c1-0451-4000-b000-000000000000")
    private val dataOutUUID = UUID.fromString("f000c0c2-0451-4000-b000-000000000000")

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

    fun readRSSI() {
        bluetoothGATT?.readRemoteRssi()
    }

    fun sendTransmissionProbes() {
        connectionManager.enqueueOperation(address, Operation.CharacteristicWrite)
    }

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

    private fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    private fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    private fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    private fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

    private fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

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
                if (connectionManager.currentOperationPair?.operation == Operation.Connect
                    || connectionManager.currentOperationPair?.operation == Operation.Disconnect) {
                    connectionManager.finishOperation()
                }
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

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Wrote to characteristic $address->$uuid | value: ${
                                value.toString(
                                    Charsets.US_ASCII
                                )
                            }"
                        )
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e(
                            "BluetoothGattCallback",
                            "Write exceeded connection ATT MTU for $address->$uuid."
                        )
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Write not permitted for $address->$uuid")
                    }
                    else -> {
                        Log.e(
                            "BluetoothGattCallback",
                            "Characteristic write failed for $address->$uuid, error $status"
                        )
                    }
                }
            }
            if (connectionManager.currentOperationPair?.operation == Operation.CharacteristicWrite) {
                connectionManager.finishOperation()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                var valAsBin = "0b"
                value.forEach { valAsBin = valAsBin.plus(" " + it.toUByte().toString(2)) }
                Log.i("BluetoothGattCallback", "Characteristic $address->$uuid | value: $valAsBin")
            }
            connectionManager.enqueueOperation(address, Operation.ReadRSSI)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> Log.i(
                    "BluetoothGattCallback",
                    "Successfully written descriptor $address->${descriptor?.uuid}"
                )
            }
            if (connectionManager.currentOperationPair?.operation == Operation.EnableNotifications) {
                connectionManager.finishOperation()
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            Log.i("BluetoothGattCallback", "RSSI@$address: $rssi")
            if (connectionManager.currentOperationPair?.operation == Operation.ReadRSSI) {
                connectionManager.finishOperation()
            }
        }
    }

    fun writeCharacteristic() {
        val payload = "Hello".toByteArray(Charsets.US_ASCII)
        val characteristic =
            bluetoothGATT?.getService(serialServiceUUID)?.getCharacteristic(dataInUUID)
        val writeType = when {
            characteristic?.isWritable() == true -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic?.isWritableWithoutResponse() == true -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> Log.e("writeCharacteristic", "${characteristic?.uuid}, cannot be written to!")
        }
        bluetoothGATT.let {
            characteristic?.writeType = writeType
            characteristic?.value = payload
            bluetoothGATT?.writeCharacteristic(characteristic)
        }
    }

    private fun writeDescriptor(descriptor: BluetoothGattDescriptor?, payload: ByteArray) {
        bluetoothGATT.let {
            descriptor?.value = payload
            bluetoothGATT?.writeDescriptor(descriptor)
        }
    }

    fun enableNotifications() {
        val characteristic =
            bluetoothGATT?.getService(serialServiceUUID)?.getCharacteristic(dataOutUUID)
        val characteristicDescriptor = characteristic?.descriptors?.get(0)
        val payload = when {
            characteristic?.isIndicatable() == true -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic?.isNotifiable() == true -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e(
                    "EnableNotifications",
                    "$address->${characteristic?.uuid} does not support notifications or indications"
                )
            }
        }
        if (!bluetoothGATT?.setCharacteristicNotification(characteristic, true)!!) {
            Log.e(
                "EnableNotifications",
                "Failed to set characteristic $address->${characteristic?.uuid}"
            )
        } else {
            writeDescriptor(characteristicDescriptor, payload as ByteArray)
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