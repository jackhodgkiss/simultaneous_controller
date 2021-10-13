package com.jackhodgkiss.simultaneous_controller

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.*
import kotlin.collections.ArrayList

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
    private val operationQueue = LinkedList<OperationPair>()
    private var currentOperationPair: OperationPair? = null

    fun connectAll() {
        sensors.forEach { connect(it.address) }
    }

    private fun connect(address: String) {
        enqueueOperation(address, Operation.Connect)
        Handler(Looper.getMainLooper()).postDelayed(
            Runnable {
                enqueueOperation(address, Operation.Disconnect)
            }, 5000
        )
    }

    @Synchronized
    private fun enqueueOperation(address: String, operation: Operation) {
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
    Disconnect(2)
}

class OperationPair(val address: String, val operation: Operation)