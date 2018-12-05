package itba.edu.ar.spi_android_app.Activities.mapActivity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import itba.edu.ar.spi_android_app.Activities.utils.ReactiveFragment
import itba.edu.ar.spi_android_app.R
import com.google.android.gms.maps.SupportMapFragment

class MapFragment : ReactiveFragment(), GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.addLogAdapter(AndroidLogAdapter())
        val mapFragment = getSupportFragmentManager().findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // TODO NOW follow this https://developers.google.com/maps/documentation/android-sdk/location
    // TODO NOW also with https://developers.google.com/maps/documentation/android-sdk/map-with-marker

    //View exists here
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.map_content, container, false)

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this.context, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this.context, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map
        // TODO: Before enabling the My Location layer, you must request
        // location permission from the user. This sample does not include
        // a request for location permission.
        if (ContextCompat.checkSelfPermission(this.context!!, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            this.map!!.isMyLocationEnabled = true
            this.map!!.setOnMyLocationButtonClickListener(this)
            this.map!!.setOnMyLocationClickListener(this)
        } else {
            // Show rationale and request permission.
            Logger.e("Location permission not granted!")
        }
    }
}