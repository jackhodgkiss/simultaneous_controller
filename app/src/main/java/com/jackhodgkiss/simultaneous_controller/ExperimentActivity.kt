package com.jackhodgkiss.simultaneous_controller

import android.bluetooth.*
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
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
    private val experimentSensors = mutableListOf<ExperimentSensorItem>()
    private lateinit var bluetoothGatt: BluetoothGatt

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExperimentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manifest = intent.getParcelableExtra<ExperimentManifest>("Manifest")!!
        binding.gestureTextView.text = manifest.gesture.toString()
        setChronometer()
        binding.timeChronometer.isCountDown = true
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
        binding.timeChronometer.base = SystemClock.elapsedRealtime() + 60 * 1001
    }

    private fun connectDevices() {
        val sensorAddress = experimentSensors[0].address
        val sensorDevice = bluetoothAdapter.getRemoteDevice(sensorAddress)
        sensorDevice.connectGatt(this, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w(
                        "BluetoothGattCallback",
                        "Successfully connected to ${gatt.device.address}"
                    )
                    bluetoothGatt = gatt
                    Handler(Looper.getMainLooper()).post {
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w(
                        "BluetoothGattCallback",
                        "Successfully disconnected from ${gatt.device.address}"
                    )
                    gatt.close()
                } else {
                    Log.w(
                        "BluetoothGattCallback",
                        "Error $status encountered from ${gatt.device.address}!"
                    )
                    gatt.close()
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Wrote to characteristic $uuid | value: ${value.toString(Charsets.US_ASCII)}"
                        )
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU.")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Write not permitted for $uuid")
                    }
                    else -> {
                        Log.e(
                            "BluetoothGattCallback",
                            "Characteristic write filed for $uuid, error $status"
                        )
                    }
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> Log.d(
                    "BluetoothDescriptorWrite",
                    "Successfully written to descriptor ${descriptor?.uuid}"
                )
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {

            with(characteristic) {
                var valAsBin = "0b"
                value.forEach { it ->
                    valAsBin = valAsBin.plus(" " + it.toUByte().toString(2))
                }
                Log.d(
                    "BluetoothCharacteristic",
                    "Characteristic $uuid | value: $valAsBin"
                )
            }
            gatt.readRemoteRssi()
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            Log.d("BluetoothRSSI", "RSSI: $rssi")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w(
                    "BluetoothGattCallback",
                    "Discovered ${services.size} services for ${device.address}"
                )
                printGattTable()
                val serialServiceUUID = UUID.fromString("f000c0c0-0451-4000-b000-000000000000")
                val dataOutUUID = UUID.fromString("f000c0c2-0451-4000-b000-000000000000")
                val dataOutCharacteristic =
                    bluetoothGatt.getService(serialServiceUUID).getCharacteristic(dataOutUUID)
                enableNotifications(dataOutCharacteristic)
            }
        }
    }

    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

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

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.d("BluetoothGattTable", "No services and characteristic available")
            return
        }
        services.forEach { service ->
            val characteristicTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.d(
                "BluetoothGattTable",
                "\nService ${service.uuid}\nCharacteristics:\n$characteristicTable"
            )
        }
    }

    private fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        bluetoothGatt.let { gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        }
    }

    private fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        val characteristicDescriptor = characteristic.descriptors[0]
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e(
                    "BluetoothNotifications",
                    "${characteristic.uuid} doesn't support notifications or indications"
                )
            }
        }
        if (!bluetoothGatt.setCharacteristicNotification(characteristic, true)) {
            Log.e(
                "BluetoothNotifications",
                "setCharacteristicNotification failed for ${characteristic.uuid}"
            )
        } else {
            writeDescriptor(characteristicDescriptor, payload as ByteArray)
        }
    }

    private fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray
    ) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> error("Characteristic ${characteristic.uuid}, cannot be written to!")
        }
        bluetoothGatt.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
        }
    }

    private fun startExperiment() {
        setChronometer()
        binding.timeChronometer.start()
        val serialServiceUUID = UUID.fromString("f000c0c0-0451-4000-b000-000000000000")
        val dataInUUID = UUID.fromString("f000c0c1-0451-4000-b000-000000000000")
        val dataInCharacteristic =
            bluetoothGatt.getService(serialServiceUUID).getCharacteristic(dataInUUID)
        val payload = "Hello".toByteArray(Charsets.US_ASCII)
        writeCharacteristic(dataInCharacteristic, payload)
    }

    private fun experimentFinished() {
        binding.timeChronometer.stop()
    }
}