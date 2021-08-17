package com.jackhodgkiss.simultaneous_controller

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.jackhodgkiss.simultaneous_controller.databinding.ActivityExperimentBinding

class ExperimentActivity : AppCompatActivity() {

    lateinit var manifest: ExperimentManifest
    lateinit var binding: ActivityExperimentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExperimentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manifest = intent.getParcelableExtra<ExperimentManifest>("Manifest")!!
        binding.gestureTextView.text = manifest.gesture.toString()
        binding.timeTextView.text = "Time: ${manifest.experimentDuration}"
    }

}