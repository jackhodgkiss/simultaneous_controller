package com.jackhodgkiss.simultaneous_controller

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity

class ExperimentManifest(
    var keyGenerationMode: KeyGenerationMode = KeyGenerationMode.SIMULTANEOUS,
    var quantizationFunction: QuantizationFunction = QuantizationFunction.TWO_LEVEL,
    var experimentDuration: ExperimentDuration = ExperimentDuration.THIRTY_SECONDS,
    var gesture: Gesture = Gesture.STATIONARY,
    var split: Split = Split.NO,
    var selectedSensors: ArrayList<String> = ArrayList<String>()
) {
    fun showAlertMessage(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Experiment Manifest")
        builder.setMessage("The following key generation and agreement experiment will done in ${keyGenerationMode.name} with a ${quantizationFunction.name} quantization function. During which the ${gesture.name} will be performed ${experimentDuration.name}. The following sensors will be used: ${selectedSensors.joinToString(", ")}")
        builder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val intent = Intent(context, ExperimentActivity::class.java)
                startActivity(context, intent, null)
                dialog?.cancel()
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

enum class KeyGenerationMode(val id: Int) {
    SIMULTANEOUS(1),
    CONSECUTIVELY(2)
}

enum class QuantizationFunction(val id: Int) {
    TWO_LEVEL(3),
    MULTI_LEVEL(4)
}

enum class ExperimentDuration(val id: Int) {
    THIRTY_SECONDS(5),
    SIXTY_SECONDS(6),
    NINETY_SECONDS(7)
}

enum class Gesture {
    STATIONARY,
    FIGURE_EIGHT,
    SHAKING_LIGHT,
    SHAKING_HEAVY,
    TILTING,
    HOLDING,
    MOVING_TOWARDS_AND_AWAY
}

enum class Split {
    YES,
    NO
}