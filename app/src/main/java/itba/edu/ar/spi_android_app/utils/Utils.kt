package ar.edu.itba.spi_android_app.utils

import android.app.Activity
import android.net.wifi.ScanResult
import android.support.v4.widget.SwipeRefreshLayout

fun SwipeRefreshLayout.stopUI(activity: Activity) = activity.runOnUiThread { this.isRefreshing = false }

/**
 * Convert a [ScanResult] to a map of BSSID => RSSI.
 */
fun scanResultToFingerprint(scanResults: List<ScanResult>) : Map<String, Double> {
    val result = HashMap<String, Double>()
    scanResults.forEach { scan ->
        result[scan.BSSID] = scan.level.toDouble()
    }
    return result
}
