package ar.edu.itba.spi_android_app.utils

import android.graphics.Bitmap
import ar.edu.itba.spi_android_app.api.models.Building
import ar.edu.itba.spi_android_app.api.models.Overlay
import ar.edu.itba.spi_android_app.api.models.Sample
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

fun buildingLatLng(building: Building): LatLng {
    return LatLng(building.latitude!!, building.longitude!!)
}

fun gMapsGroundOverlayOptions(overlay: Overlay, image: Bitmap): GroundOverlayOptions {
    return GroundOverlayOptions()
            .position(LatLng(overlay.latitude!!, overlay.longitude!!), overlay.width!!.toFloat())
            .bearing(overlay.bearing!!.toFloat())
            .anchor(overlay.anchorX!!.toFloat(), overlay.anchorY!!.toFloat())
            .image(BitmapDescriptorFactory.fromBitmap(image))
}

fun gMapsMarkerOptions(sample: Sample): MarkerOptions {
    return MarkerOptions()
            .position(LatLng(sample.latitude!!, sample.longitude!!))
            .visible(true)
            .draggable(false)
}
