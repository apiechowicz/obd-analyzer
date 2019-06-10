package com.example.obdanalyzer.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.obdanalyzer.R

class BluetoothDeviceArrayAdapter(context: Context, resource: Int) : ArrayAdapter<BluetoothDevice>(context, resource) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false)
            viewHolder =
                ViewHolder(view.findViewById(R.id.device_text) as TextView)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }
        setViewHolderText(position, viewHolder)
        return view
    }

    private fun setViewHolderText(position: Int, viewHolder: ViewHolder) {
        getItem(position)?.let {
            viewHolder.textView.text = getDeviceText(it)
        }
    }

    private fun getDeviceText(device: BluetoothDevice): String {
        return context.getString(R.string.device_text, device.name, device.address)
    }

    private class ViewHolder(internal val textView: TextView)
}
