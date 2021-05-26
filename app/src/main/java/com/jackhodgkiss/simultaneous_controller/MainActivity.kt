package com.jackhodgkiss.simultaneous_controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.jackhodgkiss.simultaneous_controller.databinding.ActivityMainBinding
import com.jackhodgkiss.simultaneous_controller.fragments.ExperimentPlannerFragment
import com.jackhodgkiss.simultaneous_controller.fragments.ScannerFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val scannerFragment = ScannerFragment()
        val experimentPlannerFragment = ExperimentPlannerFragment()

        setCurrentFragment(scannerFragment)

        binding.navigationBar.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.ic_baseline_bluetooth_searching -> setCurrentFragment(scannerFragment)
                R.id.ic_baseline_science -> setCurrentFragment(experimentPlannerFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_wrapper, fragment)
            commit()
        }
    }
}