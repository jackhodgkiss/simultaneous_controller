package com.jackhodgkiss.simultaneous_controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.SelectedSensorItemBinding

class SelectedSensorAdapter(private val sensors: ArrayList<SelectedSensorItem>) : RecyclerView.Adapter<SelectedSensorAdapter.SelectedSensorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedSensorViewHolder {
        val itemBinding =
            SelectedSensorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectedSensorViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: SelectedSensorViewHolder, position: Int) {
        holder.bindSensor(sensors[position])
    }

    override fun getItemCount() = sensors.size

    override fun getItemId(position: Int): Long {
        return sensors[position].hashCode().toLong()
    }

    class SelectedSensorViewHolder(private val itemBinding: SelectedSensorItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindSensor(selectedSensorItem: SelectedSensorItem) {
            var selectedCheckbox = itemBinding.selectedCheckbox
            itemBinding.sensorNameTextView.text = selectedSensorItem.name
            itemBinding.sensorAddressTextView.text = selectedSensorItem.address
            itemBinding.selectedCheckbox.isChecked = false
        }
    }
}