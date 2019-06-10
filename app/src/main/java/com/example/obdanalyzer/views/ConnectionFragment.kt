package com.example.obdanalyzer.views

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.obdanalyzer.R
import com.example.obdanalyzer.asString
import com.example.obdanalyzer.bluetooth.BluetoothDeviceArrayAdapter
import com.example.obdanalyzer.bluetooth.ConnectThread
import com.example.obdanalyzer.showToast
import kotlinx.android.synthetic.main.fragment_connection.view.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ConnectionFragment : Fragment() {

    private lateinit var pairedDevicesAdapter: BluetoothDeviceArrayAdapter
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_ON -> {
                        getMainFragment(context).bluetooth_toggle.isChecked = true
                        addPairedDevices(BluetoothAdapter.getDefaultAdapter())
                    }
                    BluetoothAdapter.STATE_OFF -> {
                        getMainFragment(context).bluetooth_toggle.isChecked = false
                        clearPairedDevices()
                        cleanupConnectionIfRequired()
                    }
                }
            }
        }
    }
    private var connectionThread: ConnectThread? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_connection, container, false)
        pairedDevicesAdapter = BluetoothDeviceArrayAdapter(rootView.context, android.R.layout.simple_list_item_1)
        rootView.paired_devices_list.adapter = pairedDevicesAdapter
        rootView.paired_devices_list.setOnItemClickListener { _, _, position, _ ->
            cleanupConnectionIfRequired()
            pairedDevicesAdapter.getItem(position)?.let {
                showToast(context, "connecting to ${it.name}")
                connectionThread = ConnectThread(it, context)
                connectionThread?.start()
            }
        }
        configureView(rootView)
        return rootView
    }

    private fun cleanupConnectionIfRequired() {
        connectionThread?.let {
            showToast(context, "disconnecting...")
            it.stopThread()
            LOG.info("Connection to ${it.device.asString()} cleaned up")
            showToast(context, "disconnected")
        }
    }

    override fun onResume() {
        super.onResume()
        configureView(getMainFragment(context))
        activity?.registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(bluetoothReceiver)
    }

    private fun getMainFragment(context: Context?) =
        (context as Activity).findViewById<LinearLayout>(R.id.connection_fragment)

    private fun configureView(rootView: View) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        configureBluetoothToggle(rootView, bluetoothAdapter)
        clearPairedDevices()
        addPairedDevices(bluetoothAdapter)
    }

    private fun clearPairedDevices() {
        pairedDevicesAdapter.clear()
    }

    private fun addPairedDevices(bluetoothAdapter: BluetoothAdapter) {
        bluetoothAdapter.bondedDevices.forEach(pairedDevicesAdapter::add)
    }

    private fun configureBluetoothToggle(rootView: View, bluetoothAdapter: BluetoothAdapter) {
        rootView.bluetooth_toggle.isChecked = bluetoothAdapter.isEnabled
        rootView.bluetooth_toggle.setOnClickListener {
            if (rootView.bluetooth_toggle.isChecked) {
                bluetoothAdapter.enable()
            } else {
                bluetoothAdapter.disable()
            }
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(ConnectionFragment::class.java)
        fun newInstance(): ConnectionFragment {
            return ConnectionFragment()
        }
    }
}
