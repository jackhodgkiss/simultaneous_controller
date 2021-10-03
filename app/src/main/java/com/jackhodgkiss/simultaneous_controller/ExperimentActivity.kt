package com.jackhodgkiss.simultaneous_controller

import android.bluetooth.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.ActivityExperimentBinding
import kotlin.math.log

class ExperimentActivity : AppCompatActivity() {
    private lateinit var manifest: ExperimentManifest
    private lateinit var binding: ActivityExperimentBinding
    private lateinit var experimentSensorRecyclerView: RecyclerView
    private lateinit var experimentSensorAdapter: ExperimentSensorAdapter
    private var sensorGatt: BluetoothGatt? = null
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

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

    private fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    private fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    private fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    private fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }
        sensorGatt.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt?.writeCharacteristic(characteristic)
        } ?: error("Not connected to a BLE Device!")
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE/GattCallback", "Successfully Connected to ${gatt?.device?.address}")
                    sensorGatt = gatt
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(
                        "BLE/GattCallback",
                        "Successfully Disconnected from ${gatt?.device?.address}"
                    )
                    gatt?.close()
                }
            } else {
                Log.d("BLE/GattCallback", "Error $status Encountered for ${gatt?.device?.address}")
                gatt?.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.d("BLE/ServicesDiscovered", "Discovering Services")
            val services = gatt?.services
            if (services != null) {
                if (services.isEmpty()) {
                    return
                }
                writeCharacteristic(services[services.size - 1].characteristics[0], byteArrayOf(72, 101, 108, 108, 111))
                services.forEach { service ->
                    val characteristicTable = service.characteristics.joinToString(
                        separator = "\n|--",
                        prefix = "|--"
                    ) { it.uuid.toString() }
                    Log.d(
                        "BLE/GattTable",
                        "\nService ${service.uuid}\nCharacteristics:\n$characteristicTable"
                    )
                }
            }
        }
    }

    private fun startExperiment() {
        setChronometer()
        binding.timeChronometer.start()
    }

    private fun experimentFinished() {

    }
}