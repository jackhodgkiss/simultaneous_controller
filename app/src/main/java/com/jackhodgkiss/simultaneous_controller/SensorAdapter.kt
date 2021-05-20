package com.jackhodgkiss.simultaneous_controller

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.*
import android.widget.EditText
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
        RecyclerView.ViewHolder(itemBinding.root), View.OnCreateContextMenuListener {
        private var favouriteButton = itemBinding.favouriteButton
        private val sharedPreferences = favouriteButton.context.getSharedPreferences(
            R.string.preference_file_key.toString(),
            Context.MODE_PRIVATE
        )
        private lateinit var sensor: SensorItem

        init {
            itemBinding.root.setOnCreateContextMenuListener(this)
        }

        fun bindSensor(sensor: SensorItem) {
            this.sensor = sensor
            itemBinding.sensorNameTextView.text = sensor.name
            itemBinding.sensorAddressTextView.text = sensor.address
            itemBinding.sensorRssiTextView.text =
                "RSSI: " + sensor.current_rssi.toString() + "dB"
            if (sensor.is_favourite) {
                favouriteButton.setImageResource(R.drawable.ic_favourite)
            }
            favouriteButton.setOnClickListener {
                if (sensor.is_favourite) {
                    sensor.is_favourite = false
                    favouriteButton.setImageResource(R.drawable.ic_not_favourite)
                    with(sharedPreferences.edit()) {
                        remove(sensor.address + "_is_favourite")
                        apply()
                    }
                } else {
                    sensor.is_favourite = true
                    favouriteButton.setImageResource(R.drawable.ic_favourite)
                    with(sharedPreferences.edit()) {
                        putBoolean(sensor.address + "_is_favourite", true)
                        apply()
                    }
                }
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val editName = menu?.add("Edit Name")
            val identify = menu?.add("Identify")
            editName?.setOnMenuItemClickListener(clickListener)
            identify?.setOnMenuItemClickListener(clickListener)
        }

        private val clickListener: MenuItem.OnMenuItemClickListener =
            object : MenuItem.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        0 -> editSensorName()
                        1 -> Log.d("Menu", "Identify $itemId")
                        else -> return false
                    }
                    return true
                }
            }

        private fun editSensorName() {
            val builder = AlertDialog.Builder(itemBinding.root.context)
            val input = EditText(itemBinding.root.context)
            builder.setTitle("Edit Name")
            builder.setView(input)
            builder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    sensor.name = input.text.toString()
                    itemBinding.sensorNameTextView.text = sensor.name
                    with(sharedPreferences.edit()) {
                        putString(sensor.address + "_name", sensor.name)
                        apply()
                    }
                }
            })
            builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.cancel()
                }
            })
            builder.show()
        }

    }
}