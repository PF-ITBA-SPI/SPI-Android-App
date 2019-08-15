package ar.edu.itba.spi_android_app.api.models

import android.net.wifi.ScanResult
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Sample: Serializable {
    @SerializedName("_id")
    var _id: String? = null

    @SerializedName("buildingId")
    var buildingId: String? = null

    @SerializedName("floorId")
    var floorId: String? = null

    @SerializedName("latitude")
    var latitude: Double? = null

    @SerializedName("longitude")
    var longitude: Double? = null

    @SerializedName("fingerprint")
    val fingerprint = HashMap<String, Double>()

    @SerializedName("extra")
    val extraData = HashMap<String, Any>()

    constructor(buildingId: String, floorId: String, latitude: Double, longitude: Double, scanResults: List<ScanResult>) {
        this.buildingId = buildingId
        this.floorId = floorId
        this.latitude = latitude
        this.longitude = longitude
        scanResults.forEach { result ->
            fingerprint[result.BSSID] = result.level.toDouble()
            extraData[result.BSSID] = extraDataMap(result)
        }
    }

    private fun extraDataMap(scanResult : ScanResult) : Map<String, Any> {
        val result = HashMap<String, Any>()
        result["SSID"] = scanResult.SSID
        result["capabilities"] = scanResult.capabilities
//        result["centerFreq0"] = scanResult.centerFreq0
//        result["centerFreq1"] = scanResult.centerFreq1
//        result["channelWidth"] = scanResult.channelWidth
        result["frequency"] = scanResult.frequency
//        result["is80211mcResponder"] = scanResult.is80211mcResponder
//        result["isPasspointNetwork"] = scanResult.isPasspointNetwork
//        result["operatorFriendlyName"] = scanResult.operatorFriendlyName
//        result["venueName"] = scanResult.venueName
        return result
    }

    override fun toString(): String {
        return "Sample for floor $floorId of building $buildingId"
    }
}

