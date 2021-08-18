package com.jackhodgkiss.simultaneous_controller.fragments

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.*
import com.jackhodgkiss.simultaneous_controller.databinding.FragmentExperimentPlannerBinding

class ExperimentPlannerFragment : Fragment() {
    private val selectableSensors: ArrayList<SelectableSensorItem> = ArrayList()
    private lateinit var binding: FragmentExperimentPlannerBinding
    private lateinit var gestureSpinner: Spinner
    private lateinit var selectableSensorsRecyclerView: RecyclerView
    private lateinit var selectableSensorsAdapter: SelectableSensorAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExperimentPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gestureSpinner = binding.gestureSpinner
        ArrayAdapter.createFromResource(
            view.context,
            R.array.gestures_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            gestureSpinner.adapter = adapter
        }
        selectableSensorsRecyclerView = binding.selectableSensorsRecyclerView
        selectableSensorsAdapter = SelectableSensorAdapter(selectableSensors)
        selectableSensorsAdapter.setHasStableIds(true)
        selectableSensorsRecyclerView.adapter = selectableSensorsAdapter
        selectableSensorsRecyclerView.layoutManager = LinearLayoutManager(view.context)
        selectableSensorsRecyclerView.itemAnimator = null
        binding.refreshSelectableSensorsButton.setOnClickListener {
            requestPermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            )
        }
        sharedPreferences =
            view.context.getSharedPreferences(
                R.string.preference_file_key.toString(),
                Context.MODE_PRIVATE
            )
        binding.confirmButton.setOnClickListener {
            confirmExperiment()
        }
    }

    private fun confirmExperiment() {
        val experimentManifest = ExperimentManifest()
        val radioGroups = arrayOf(
            binding.keyGenerationModeRadioGroup,
            binding.quantizationFunctionRadioGroup,
            binding.experimentDurationRadioGroup
        )
        radioGroups.forEach { element ->
            when (element.checkedRadioButtonId) {
                1 -> experimentManifest.keyGenerationMode = KeyGenerationMode.SIMULTANEOUS
                2 -> experimentManifest.keyGenerationMode = KeyGenerationMode.CONSECUTIVELY
                3 -> experimentManifest.quantizationFunction = QuantizationFunction.TWO_LEVEL
                4 -> experimentManifest.quantizationFunction = QuantizationFunction.MULTI_LEVEL
                5 -> experimentManifest.split = Split.YES
                6 -> experimentManifest.split = Split.NO
                7 -> experimentManifest.experimentDuration = ExperimentDuration.THIRTY_SECONDS
                8 -> experimentManifest.experimentDuration = ExperimentDuration.SIXTY_SECONDS
                9 -> experimentManifest.experimentDuration = ExperimentDuration.NINETY_SECONDS
            }
        }
        experimentManifest.gesture = Gesture.values()[binding.gestureSpinner.selectedItemId.toInt()]
        selectableSensors.forEach { sensor ->
            if (sensor.isSelected) {
                experimentManifest.selectedSensors[sensor.address] = sensor.name
            }
        }
        context?.let { experimentManifest.showAlertMessage(it) }
    }

    private fun scanForDevices(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bluetoothAdapter.bluetoothLeScanner.startScan(null, settings, callback)
        Handler(Looper.getMainLooper()).postDelayed({
            bluetoothAdapter.bluetoothLeScanner.stopScan(callback)
        }, 5_000)
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
        if (!selectableSensors.any { sensor -> sensor.address == result.device.address }) {
            if (sharedPreferences.getBoolean(result.device.address + "_is_favourite", false)) {
                if (result.device.name != null) {
                    val sensorName =
                        sharedPreferences.getString(
                            result.device.address + "_name",
                            result.device.name
                        )
                            .toString()
                    val sensor = SelectableSensorItem(
                        sensorName,
                        result.device.address,
                        false
                    )
                    selectableSensors.add(sensor)
                    selectableSensorsAdapter.notifyItemChanged(selectableSensors.size - 1)
                }
            }
        }
    }

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if(permissions.values.all { value -> value == true}) {
            view?.let { scanForDevices(it.context) }
        } else {
            Toast.makeText(view?.context, "Permissions Not Granted!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val REQUEST_ENABLE_BT: Int = 1
    }

}