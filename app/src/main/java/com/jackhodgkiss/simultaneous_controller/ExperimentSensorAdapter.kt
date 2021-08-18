package com.jackhodgkiss.simultaneous_controller

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.ExperimentSensorItemBinding

class ExperimentSensorAdapter(private val sensors: ArrayList<ExperimentSensorItem>) :
    RecyclerView.Adapter<ExperimentSensorAdapter.ExperimentSensorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperimentSensorViewHolder {
        val itemBinding =
            ExperimentSensorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExperimentSensorViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ExperimentSensorViewHolder, position: Int) {
        holder.bindSensor(sensors[position])
    }

    override fun getItemCount() = sensors.size

    override fun getItemId(position: Int) = sensors[position].hashCode().toLong()

    class ExperimentSensorViewHolder(private val itemBinding: ExperimentSensorItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindSensor(experimentSensorItem: ExperimentSensorItem) {
            val sharedPreferences = itemBinding.sensorNameTextView.context.getSharedPreferences(
                R.string.preference_file_key.toString(),
                Context.MODE_PRIVATE
            )
            itemBinding.sensorNameTextView.text =
                sharedPreferences.getString(experimentSensorItem.address + "_name", "N/A")
        }
    }
}