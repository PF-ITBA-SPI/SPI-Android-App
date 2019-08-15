package ar.edu.itba.spi_android_app.api.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Floor: Serializable {
    @SerializedName("_id")
    var _id: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("number")
    val number: Int? = null

    @SerializedName("overlay")
    val overlay: Overlay? = null

    override fun toString(): String {
        return "$name"
    }
}

