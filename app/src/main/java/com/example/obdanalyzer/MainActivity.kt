package com.example.obdanalyzer

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter
import com.example.obdanalyzer.views.ConnectionFragment
import com.example.obdanalyzer.views.DataFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MainActivity : AppCompatActivity() {

    /**
     * The [androidx.viewpager.widget.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.fragment.app.FragmentStatePagerAdapter.
     */
    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
        checkPermissions()
        log.info("MainActivity created")
    }

    override fun onResume() {
        super.onResume()
        if (intent?.extras?.getBoolean(SWITCH_TO_DATA_FRAGMENT) == true) {
            container.currentItem = 1
            intent.removeExtra(SWITCH_TO_DATA_FRAGMENT)
        }
    }

    private fun checkPermissions() {
        Constants.REQUIRED_PERMISSIONS.filter { isPermissionDenied(it) }.let {
            if (it.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, it.toTypedArray(), startupPermissionRequestId)
            }
        }
    }

    private fun isPermissionDenied(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        for (index in 0 until permissions.size) {
            val permission = permissions[index]
            val result = grantResults[index]
            println(permission)
            if (permission == Constants.WRITE_EXTERNAL_STORAGE && result == PackageManager.PERMISSION_GRANTED) {
                reinitLogger()
            }
        }
    }

    private fun reinitLogger() {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        // Copy current loggers, necessary to keep logging.
        val loggers = loggerContext.copyOfListenerList
        loggerContext.reset()
        val config = JoranConfigurator()
        config.context = loggerContext
        val inputStream = assets.open("logback.xml")
        config.doConfigure(inputStream)
        loggers.forEach { loggerContext.addListener(it) } // restore loggers
        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext)
        log.info("Logger has been reset")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun logButtonClick(view: View) {
        log.info("${(view as? Button)?.text}")
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MainActivity::class.java)
        private const val startupPermissionRequestId = 1
        const val SWITCH_TO_DATA_FRAGMENT = "SWITCH_TO_DATA_FRAGMENT"
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> ConnectionFragment.newInstance()
            1 -> DataFragment.newInstance(position)
            else -> error("View with given number does not exist")
        }

        override fun getCount(): Int {
            return 2
        }
    }
}
