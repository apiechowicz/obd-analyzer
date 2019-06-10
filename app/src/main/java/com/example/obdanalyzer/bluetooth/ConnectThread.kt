package com.example.obdanalyzer.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import com.example.obdanalyzer.MainActivity
import com.example.obdanalyzer.asString
import com.example.obdanalyzer.obd2.Obd2Connection
import com.example.obdanalyzer.showToast
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class ConnectThread(val device: BluetoothDevice, private val context: Context?) : Thread() {
    private val running = AtomicBoolean()
    private lateinit var obd2Connection: Obd2Connection

    override fun run() {
        running.set(true)
        if (adapterIsNotDiscovering()) {
            device.uuids.find { it.uuid == OBD2_UUID }?.let {
                createAndManageConnection()
            } ?: run {
                showToast(context, "device doesn't support OBD2 interface")
                LOG.warn("${device.asString()} doesn't support OBD2 server UUID")
            }
        }
    }

    private fun adapterIsNotDiscovering(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return !bluetoothAdapter.isDiscovering || bluetoothAdapter.cancelDiscovery()
    }

    private fun createAndManageConnection() {
        device.createRfcommSocketToServiceRecord(OBD2_UUID).use { socket ->
            if (!socket.isConnected) {
                try {
                    socket.connect()
                    LOG.info("Bluetooth connection to ${device.asString()}. Connected? ${socket.isConnected}")
                    showToast(context, "connected")
                    obd2Connection = Obd2Connection(socket.outputStream, socket.inputStream, running)
                    if (obd2Connection.performInitializationSequence()) {
                        showToast(context, "init sequence succeeded")
                        LOG.info("OBD initialization sequence succeeded")
                        context?.startActivity(createSwitchFragmentIntent())
                        obd2Connection.transmitData()
                        showToast(context, "connection closed")
                        LOG.info("Connection closed")
                    } else {
                        showToast(context, "init sequence failed")
                        LOG.error("OBD initialization sequence failed")
                    }
                } catch (e: IOException) {
                    showToast(context, "failed to connect to ${device.name}")
                    LOG.error("Failed to connect to ${device.asString()}", e)
                }
            }
        }
    }

    private fun createSwitchFragmentIntent(): Intent {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(MainActivity.SWITCH_TO_DATA_FRAGMENT, true)
        return intent
    }

    fun stopThread() {
        running.set(false)
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(ConnectThread::class.java)
        private val OBD2_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
