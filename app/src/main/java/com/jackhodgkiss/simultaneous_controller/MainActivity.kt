package com.jackhodgkiss.simultaneous_controller

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scanForDevices(this)
    }

    fun scanForDevices(context: Context) {
        context.runWithPermissions(Manifest.permission.ACCESS_COARSE_LOCATION) {
            Log.d("Permissions", "Permission to access coarse location granted")
        }
    }
}