package com.jackhodgkiss.simultaneous_controller

import android.content.Context

class ConnectionManager(
    val context: Context,
    private val addresses: List<String>,
) {
    var sensors = addresses.map { address ->
        ExperimentSensor(
            context,
            address,
            this
        )
    } as ArrayList<ExperimentSensor>

    fun connect() {
        sensors.forEach { it.connect() }
    }
}