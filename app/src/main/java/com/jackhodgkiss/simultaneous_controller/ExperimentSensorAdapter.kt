package com.jackhodgkiss.simultaneous_controller

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.ExperimentSensorItemBinding

class ExperimentSensorAdapter(private val connectionManager: ConnectionManager) :
    RecyclerView.Adapter<ExperimentSensorAdapter.ExperimentSensorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperimentSensorViewHolder {
        val itemBinding =
            com.jackhodgkiss.simultaneous_controller.databinding.ExperimentSensorItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ExperimentSensorViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ExperimentSensorViewHolder, position: Int) {
        holder.bindSensor(connectionManager.sensors[position])
    }

    override fun getItemCount() = connectionManager.sensors.size

    override fun getItemId(position: Int) = connectionManager.sensors[position].hashCode().toLong()

    class ExperimentSensorViewHolder(private val itemBinding: ExperimentSensorItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindSensor(experimentSensor: ExperimentSensor) {
            itemBinding.sensorNameTextView.text = experimentSensor.address
            if(experimentSensor.isConnected) {
                itemBinding.bluetoothConnectedImageView.visibility = ImageView.VISIBLE
            } else {
                itemBinding.bluetoothConnectedImageView.visibility = ImageView.INVISIBLE
            }
        }
    }
}