package com.jackhodgkiss.simultaneous_controller

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.SensorItemBinding

class SensorAdapter(private val sensors: ArrayList<SensorItem>) : RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val itemBinding = SensorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SensorViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        holder.bindSensor(sensors[position])
    }

    override fun getItemCount() = sensors.size

    class SensorViewHolder(private var itemBinding: SensorItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bindSensor(sensor: SensorItem) {
            itemBinding.sensorNameTextView.text = sensor.name
            itemBinding.sensorAddressTextView.text = sensor.address
        }
    }

}