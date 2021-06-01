package com.jackhodgkiss.simultaneous_controller.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.jackhodgkiss.simultaneous_controller.R
import com.jackhodgkiss.simultaneous_controller.databinding.FragmentExperimentPlannerBinding
import com.jackhodgkiss.simultaneous_controller.databinding.FragmentScannerBinding

class ExperimentPlannerFragment : Fragment() {
    private lateinit var binding: FragmentExperimentPlannerBinding
    private lateinit var gesture_spinner: Spinner


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExperimentPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gestureSpinner = binding.gestureSpinner
        ArrayAdapter.createFromResource(
            view.context,
            R.array.gestures_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            gestureSpinner.adapter = adapter
        }
    }

}