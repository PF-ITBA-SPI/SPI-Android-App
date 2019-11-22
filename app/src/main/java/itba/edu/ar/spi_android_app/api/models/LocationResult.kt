package ar.edu.itba.spi_android_app.api.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class LocationResult: Serializable {
    @SerializedName("buildingId")
    var buildingId: String? = null

    @SerializedName("floorId")
    var floorId: String? = null

    @SerializedName("latitude")
    var latitude: Double? = null

    @SerializedName("longitude")
    var longitude: Double? = null

    fun isComplete(): Boolean {
        return buildingId != null && floorId != null && latitude != null && longitude != null;
    }

    override fun toString(): String {
        return "LocationResult @ ($latitude, $longitude) on floor ${floorId ?: "?"} of building ${buildingId ?: "?"}"
    }
}

