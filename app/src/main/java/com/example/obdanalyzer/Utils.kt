package com.example.obdanalyzer

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.widget.Toast

fun showToast(context: Context?, text: String) =
    (context as? Activity)?.let { it.runOnUiThread { Toast.makeText(it, text, Toast.LENGTH_SHORT).show() } }

fun BluetoothDevice.asString(): String = "Device(name='$name', address=$address)"
