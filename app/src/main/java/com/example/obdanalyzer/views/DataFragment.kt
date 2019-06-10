package com.example.obdanalyzer.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.obdanalyzer.R
import com.example.obdanalyzer.obd2.CurrentDataCommand
import com.example.obdanalyzer.obd2.DataProvider
import com.example.obdanalyzer.obd2.Obd2Data
import com.example.obdanalyzer.obd2.Obd2ResponseParser
import com.example.obdanalyzer.showToast
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_data.view.*

/**
 * A placeholder fragment containing a simple view.
 */
class DataFragment : Fragment() {

    private lateinit var speedTextView: TextView
    private lateinit var rpmTextView: TextView
    private lateinit var loadTextView: TextView
    private lateinit var subscription: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_data, container, false)
        speedTextView = rootView.speed_text_view
        rpmTextView = rootView.rpm_text_view
        loadTextView = rootView.load_text_view
        return rootView
    }

    override fun onStart() {
        super.onStart()
        subscription = DataProvider.getDataObservable().subscribe(this::consumeNewData)
    }

    private fun consumeNewData(data: Obd2Data) {
        if (userVisibleHint) {
            activity?.runOnUiThread {
                when (data.command) {
                    CurrentDataCommand.VEHICLE_SPEED -> speedTextView.text = data.value
                    CurrentDataCommand.ENGINE_RPM -> {
                        rpmTextView.text = data.value
                        displayGearTip(data)
                    }
                    CurrentDataCommand.ENGINE_LOAD -> loadTextView.text = data.value
                    else -> error("Unknown obd command type")
                }
            }
        }
    }

    private fun displayGearTip(data: Obd2Data) {
        if (data.value != Obd2ResponseParser.NO_DATA) {
            val speed = speedTextView.text.toString()
            if (speed != Obd2ResponseParser.NO_DATA && speed.toInt() >= MIN_SPEED) {
                val rpm = data.value.toInt()
                when {
                    rpm <= LOW_RPM -> showToast(context, "reduce gear")
                    rpm >= HIGH_RPM -> showToast(context, "increase gear")
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        subscription.dispose()
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"
        private const val MIN_SPEED = 10
        private const val LOW_RPM = 2_000
        private const val HIGH_RPM = 3_000

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int): DataFragment {
            val fragment = DataFragment()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }
}
