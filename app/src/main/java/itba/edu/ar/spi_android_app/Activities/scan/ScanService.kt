package itba.edu.ar.spi_android_app.Activities.scan

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import ar.edu.itba.spi_android_app.utils.TAG

/**
 * Created by julianrodrigueznicastro on 22/08/2019.
 */
class ScanService(private var activity: Activity) {

    private val MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE = 42
    private lateinit var wifiManager: WifiManager
    private var resultList = MutableLiveData<List<ScanResult>>()


    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                Log.d(TAG, wifiManager.scanResults.toString())
                val newResults = ArrayList<ScanResult>()
                wifiManager.scanResults.forEach { result ->
                    if(result != null) newResults.add(result)
                }
                resultList.value = newResults
            } else {
                Log.d(TAG, "Scan Failed")
            }
        }
    }

    init {
        this.activity = activity
        wifiManager = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        activity.registerReceiver(wifiScanReceiver, intentFilter)
    }


    fun askPermissions(): Boolean {
        Log.d(TAG, "CHECKING PERMISSIONS")
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CHANGE_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            Log.d(TAG, "ASKING FOR PERMISSIONS")
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE)
        }
        return false
    }

    fun getLiveResults(): MutableLiveData<List<ScanResult>> { return resultList }


    fun startScanning() {
        if(askPermissions()){
            Log.d(TAG, "START SCAN")
            wifiManager.startScan()
        }
        Handler().postDelayed({
            startScanning()
        }, 30000)
    }
}
