package com.jackhodgkiss.simultaneous_controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class ExperimentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experiment)
        val intent = intent
        val manifest = intent.getParcelableExtra<ExperimentManifest>("Manifest")
        Log.d("Experiment", manifest?.quantizationFunction.toString())
    }
}