package com.jackhodgkiss.simultaneous_controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.SelectableSensorItemBinding

class SelectableSensorAdapter(private val sensors: ArrayList<SelectableSensorItem>) : RecyclerView.Adapter<SelectableSensorAdapter.SelectableSensorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableSensorViewHolder {
        val itemBinding =
            SelectableSensorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectableSensorViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: SelectableSensorViewHolder, position: Int) {
        holder.bindSensor(sensors[position])
    }

    override fun getItemCount() = sensors.size

    override fun getItemId(position: Int): Long {
        return sensors[position].hashCode().toLong()
    }

    class SelectableSensorViewHolder(private val itemBinding: SelectableSensorItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindSensor(selectableSensorItem: SelectableSensorItem) {
            val selectedCheckbox = itemBinding.selectedCheckbox
            itemBinding.sensorNameTextView.text = selectableSensorItem.name
            itemBinding.sensorAddressTextView.text = selectableSensorItem.address
            itemBinding.selectedCheckbox.isChecked = false
            selectedCheckbox.setOnClickListener {
                selectableSensorItem.isSelected = selectedCheckbox.isChecked
            }
        }
    }
}