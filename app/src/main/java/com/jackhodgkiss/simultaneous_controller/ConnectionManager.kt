package com.jackhodgkiss.simultaneous_controller

import android.content.Context
import android.util.Log
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class ConnectionManager(
    val context: Context,
    private val addresses: List<String>,
) {
    var sensors = addresses.map {
        ExperimentSensor(
            context,
            it,
            this
        )
    } as ArrayList<ExperimentSensor>
    private val operationQueue = ConcurrentLinkedQueue<OperationPair>()
    var currentOperationPair: OperationPair? = null

    fun connectAll() {
        sensors.forEach { connect(it.address) }
    }

    private fun connect(address: String) {
        enqueueOperation(address, Operation.Connect)
        enqueueOperation(address, Operation.DiscoverServices)
        enqueueOperation(address, Operation.EnableNotifications)
    }

    fun sendTransmissionProbes() {
        var index = 0
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                index++
                Log.d("sendTransmissionProbes", "$index")
                if(index >= 10) { cancel() }
            }
        }, 2, 10)
    }

    @Synchronized
    fun enqueueOperation(address: String, operation: Operation) {
        operationQueue.add(OperationPair(address, operation))
        if (currentOperationPair == null) {
            nextOperation()
        }
    }

    @Synchronized
    private fun nextOperation() {
        if (currentOperationPair != null) {
            return
        }
        val nextOperation = operationQueue.poll() ?: run { return }
        currentOperationPair = nextOperation
        val sensor = sensors.find { it.address == currentOperationPair!!.address }
        when (currentOperationPair!!.operation) {
            Operation.Connect -> {
                sensor?.connect()
            }
            Operation.Disconnect -> {
                sensor?.disconnect()
            }
            Operation.DiscoverServices -> {
                sensor?.discoverServices()
            }
            Operation.CharacteristicWrite -> {
                sensor?.writeCharacteristic()
            }
            Operation.EnableNotifications -> {
                sensor?.enableNotifications()
            }
            Operation.ReadRSSI -> {
                sensor?.readRSSI()
            }
        }
    }

    @Synchronized
    fun finishOperation() {
        currentOperationPair = null
        if(operationQueue.isNotEmpty()) {
            nextOperation()
        }
    }
}

enum class Operation(val id: Short) {
    Connect(1),
    Disconnect(2),
    DiscoverServices(3),
    CharacteristicWrite(4),
    EnableNotifications(5),
    ReadRSSI(6)
}

class OperationPair(val address: String, val operation: Operation)