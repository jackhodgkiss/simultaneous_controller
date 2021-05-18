package com.jackhodgkiss.simultaneous_controller

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.databinding.SensorItemBinding

class SensorAdapter(private val sensors: ArrayList<SensorItem>) :
    RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val itemBinding =
            SensorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SensorViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        holder.bindSensor(sensors[position])
    }

    override fun getItemCount() = sensors.size

    override fun getItemId(position: Int): Long {
        return sensors[position].hashCode().toLong()
    }

    class SensorViewHolder(private var itemBinding: SensorItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        private var favourite_button = itemBinding.favouriteButton
        private val sharedPreferences = favourite_button.context.getSharedPreferences(
            R.string.preference_file_key.toString(),
            Context.MODE_PRIVATE
        )

        fun bindSensor(sensor: SensorItem) {
            itemBinding.sensorNameTextView.text = sensor.name
            itemBinding.sensorAddressTextView.text = sensor.address
            itemBinding.sensorRssiTextView.text =
                "RSSI: " + sensor.rssi_values.average().toInt().toString() + "dB"
            if (sensor.is_favourite) {
                favourite_button.setImageResource(R.drawable.ic_favourite)
            }
            favourite_button.setOnClickListener {
                if (sensor.is_favourite) {
                    sensor.is_favourite = false
                    favourite_button.setImageResource(R.drawable.ic_not_favourite)
                    with(sharedPreferences.edit()) {
                        remove(sensor.address)
                        apply()
                    }
                } else {
                    sensor.is_favourite = true
                    favourite_button.setImageResource(R.drawable.ic_favourite)
                    with(sharedPreferences.edit()) {
                        putBoolean(sensor.address, true)
                        apply()
                    }
                }
            }
        }
    }
}