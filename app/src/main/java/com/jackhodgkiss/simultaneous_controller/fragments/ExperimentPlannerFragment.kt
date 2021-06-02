package com.jackhodgkiss.simultaneous_controller.fragments

import android.Manifest
import android.bluetooth.BluetoothAdapter
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.R
import com.jackhodgkiss.simultaneous_controller.SelectableSensorAdapter
import com.jackhodgkiss.simultaneous_controller.SelectableSensorItem
import com.jackhodgkiss.simultaneous_controller.databinding.FragmentExperimentPlannerBinding
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions

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
            scanForDevices(view.context)
        }
        sharedPreferences =
            view.context.getSharedPreferences(R.string.preference_file_key.toString(), Context.MODE_PRIVATE)
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
                selectableSensors.clear()
                selectableSensorsAdapter.notifyDataSetChanged()
                val settings =
                    ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
                adapter.bluetoothLeScanner.startScan(null, settings, callback)
                Handler(Looper.getMainLooper()).postDelayed({
                    adapter.bluetoothLeScanner.stopScan(callback)
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
        if (!selectableSensors.any { sensor -> sensor.address == result.device.address }) {
            if(sharedPreferences.getBoolean(result.device.address + "_is_favourite", false)) {
                if (result.device.name != null) {
                    val sensorName =
                        sharedPreferences.getString(result.device.address + "_name", result.device.name)
                            .toString()
                    val sensor = SelectableSensorItem(
                        sensorName,
                        result.device.address,
                        false)
                    selectableSensors.add(sensor)
                    selectableSensorsAdapter.notifyItemChanged(selectableSensors.size - 1)
                }
            }
        }
    }

    companion object {
        const val REQUEST_ENABLE_BT: Int = 1
    }

}