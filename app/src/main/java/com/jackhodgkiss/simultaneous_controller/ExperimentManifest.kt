package com.jackhodgkiss.simultaneous_controller

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat.startActivity
import kotlinx.parcelize.Parcelize

@Parcelize
class ExperimentManifest(
    var keyGenerationMode: KeyGenerationMode = KeyGenerationMode.SIMULTANEOUS,
    var gesture: Gesture = Gesture.STATIONARY,
    var selectedSensors: MutableMap<String, String> = mutableMapOf()
) : Parcelable {
    fun showAlertMessage(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Experiment Manifest")
        builder.setMessage("The following key generation and agreement experiment will done in " +
                "${keyGenerationMode.name}. During which the ${gesture.name} will be performed.")
        builder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val intent = Intent(context, ExperimentActivity::class.java)
                intent.putExtra("Manifest", this@ExperimentManifest)
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
    SIMULTANEOUS_PLUS(2),
    CONSECUTIVELY(3)
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