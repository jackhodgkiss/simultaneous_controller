package com.jackhodgkiss.simultaneous_controller.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jackhodgkiss.simultaneous_controller.R
import com.jackhodgkiss.simultaneous_controller.SelectableSensorAdapter
import com.jackhodgkiss.simultaneous_controller.SelectableSensorItem
import com.jackhodgkiss.simultaneous_controller.databinding.FragmentExperimentPlannerBinding

class ExperimentPlannerFragment : Fragment() {
    private val selectableSensors: ArrayList<SelectableSensorItem> = ArrayList()
    private lateinit var binding: FragmentExperimentPlannerBinding
    private lateinit var gestureSpinner: Spinner
    private lateinit var selectableSensorsRecyclerView: RecyclerView
    private lateinit var selectableSensorsAdapter: SelectableSensorAdapter


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
        gestureSpinner = binding.gestureSpinner
        ArrayAdapter.createFromResource(
            view.context,
            R.array.gestures_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            gestureSpinner.adapter = adapter
        }
        selectableSensorsRecyclerView = binding.selectableSensorsRecyclerView
        selectableSensorsAdapter = SelectableSensorAdapter(selectableSensors)
        selectableSensorsAdapter.setHasStableIds(true)
        selectableSensorsRecyclerView.adapter = selectableSensorsAdapter
        selectableSensorsRecyclerView.layoutManager = LinearLayoutManager(view.context)
        selectableSensorsRecyclerView.itemAnimator = null
    }
}